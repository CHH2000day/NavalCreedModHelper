package com.CHH2000day.navalcreed.modhelper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.CHH2000day.navalcreed.modhelper.ModPackageInfo.IllegalModInfoException
import com.CHH2000day.navalcreed.modhelper.ModPackageInstallHelper.onModPackageLoadDoneListener
import com.CHH2000day.navalcreed.modhelper.ModPackageManagerV2.getModTypeName
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import java.io.File

class ModPackageInstallerFragment : Fragment() {
    private var loader: UriLoader? = null
    private lateinit var v: View
    private lateinit var preview: ImageView
    private lateinit var infoView: TextView
    private lateinit var updateButton: Button
    private lateinit var selectButton: Button
    private lateinit var cancelButton: Button
    private var modPackageInstallHelper: ModPackageInstallHelper? = null
    private var isCache = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.modinfopage, null)
        preview = v.findViewById(R.id.modinfopageImageView)
        infoView = v.findViewById(R.id.modinfopageTextView)
        selectButton = v.findViewById(R.id.modinfopageButtonSelect)
        updateButton = v.findViewById(R.id.modinfopageButtonUpdate)
        cancelButton = v.findViewById(R.id.modinfopageButtonCancel)
        infoView.movementMethod = ScrollingMovementMethod()
        return v
    }

    override fun onResume() {
        super.onResume()
        if (loader != null && loader!!.uri != null) {
            selectFile(loader!!.uri)
            //注销接口防止被重复使用
            loader = null
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        selectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), QUERY_CODE)
        }
        selectButton.setOnLongClickListener {
            val pkg = "com.android.documentsui"
            val packageURI = Uri.parse("package:$pkg")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
            startActivity(intent)
            true
        }
        cancelButton.setOnClickListener { clear() }
        updateButton.setOnClickListener {
            if (modPackageInstallHelper == null) {
                Snackbar.make(v, R.string.modpkg_info_empty, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            modPackageInstallHelper!!.beginInstall(activity)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (modPackageInstallHelper != null) {
            modPackageInstallHelper!!.recycle()
            modPackageInstallHelper = null
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        loader = activity as UriLoader
    }

    private fun clear() {
        if (modPackageInstallHelper != null) {
            synchronized(modPackageInstallHelper!!) {
                if (isCache) {
                    Utils.delDir(modPackageInstallHelper!!.sourceFile)
                    isCache = false
                }
                modPackageInstallHelper!!.recycle()
            }
        }
        preview.setImageResource(R.drawable.no_preview)
        infoView.setText(R.string.modpkg_info_empty)
        modPackageInstallHelper = null
    }

    private suspend fun Activity.getSource(uri: Uri): Source? {
        return withContext(Dispatchers.IO) {
            activity!!.contentResolver.openInputStream(uri)?.source()
        }
    }

    private fun selectFile(uri: Uri?) {
        clear()
        val filepath = Utils.resolveFilePath(uri, activity)
        if (filepath == null) {
            CoroutineScope(Dispatchers.Main).launch(Dispatchers.Main) {
                val alertDialogBuilder = activity?.let { AlertDialog.Builder(it) }
                alertDialogBuilder?.setTitle(R.string.please_wait)
                alertDialogBuilder?.setMessage("Copying in alternate mode...")
                alertDialogBuilder?.setCancelable(false)
                val alertDialog = alertDialogBuilder?.create()
                alertDialog?.setCanceledOnTouchOutside(false)
                alertDialog?.show()
                var file: File? = null
                val isSuccess = withContext(Dispatchers.IO) {
                    file = File(activity!!.externalCacheDir, "cachedmodfile.ncmod")
                    var isSuccess = false
                    val sink = file!!.sink().buffer()
                    val source = activity!!.getSource(uri!!)
                    if (source != null) {
                        if (sink.writeAll(source) > 0) {
                            isCache = true
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }
                alertDialog?.dismiss()
                if (isSuccess) {
                    file?.let { load(it) }
                } else {
                    alertDialogBuilder?.setTitle(R.string.failed)
                    alertDialogBuilder?.setMessage(R.string.failed)
                    alertDialogBuilder?.setPositiveButton(R.string.ok, null)
                    alertDialogBuilder?.setCancelable(false)
                    alertDialogBuilder?.create()?.show()
                }
            }
        } else {
            isCache = false
            load(File(filepath))
        }
    }

    private fun load(source: File) {
        val adb = AlertDialog.Builder(activity!!)
        adb.setTitle(R.string.please_wait)
                .setMessage(R.string.please_wait)
                .setCancelable(false)
        val ad = adb.create()
        ad.setCanceledOnTouchOutside(false)
        ad.show()
        modPackageInstallHelper = ModPackageInstallHelper(source)
        val act: AppCompatActivity = activity as Main
        modPackageInstallHelper!!.load(object : onModPackageLoadDoneListener {
            override fun onSuccess() {
                if (!isAdded) {
                    return
                }
                val modPackageInfo = modPackageInstallHelper!!.modPackageInfo
                val modSize = modPackageInstallHelper!!.totalSize
                val sb = StringBuilder()
                sb.append(getString(R.string.modname))
                        .append(modPackageInfo.modName)
                        .append("\n")
                        .append(getString(R.string.modsize))
                        .append(Utils.convertFileSize(modSize))
                        .append("\n")
                        .append(getString(R.string.modtype))
                        .append(getModTypeName(modPackageInfo.modType))
                        .append("\n")
                        .append(getText(R.string.modauthor))
                        .append(modPackageInfo.modAuthor)
                        .append("\n")
                        .append(getString(R.string.modinfo))
                        .append(modPackageInfo.modInfo)
                if (modPackageInfo.modType == ModPackageInfo.MODTYPE_OTHER) {
                    sb.append("\n")
                            .append(getString(R.string.ununinstallable_modpkg_warning))
                }
                infoView.text = sb.toString()
                if (modPackageInfo.hasPreview()) {
                    preview.setImageBitmap(modPackageInfo.modPreview)
                }
                ad.dismiss()
            }

            override fun onFail(t: Throwable) {
                ad.setCancelable(true)
                ad.setCanceledOnTouchOutside(true)
                ad.setTitle(getString(R.string.error))
                if (t is IllegalModInfoException) {
                    ad.setMessage("${getString(R.string.invalid_mod_info)}\n${Utils.getErrMsg(t)}")
                } else {
                    ad.setMessage(Utils.getErrMsg(t))
                }
                if (isCache) {
                    Utils.delDir(source)
                }
                modPackageInstallHelper = null
            }

            override fun getActivity(): AppCompatActivity {
                return act
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (QUERY_CODE == requestCode && AppCompatActivity.RESULT_OK == resultCode && data != null) {
            if (data.data == null) {
                Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show()
            }
            selectFile(data.data)
        }
    }

    interface UriLoader {
        val uri: Uri?
    }

    companion object {
        private const val QUERY_CODE = 6
    }
}