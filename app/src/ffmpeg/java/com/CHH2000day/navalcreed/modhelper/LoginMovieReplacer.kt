package com.CHH2000day.navalcreed.modhelper

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.CHH2000day.navalcreed.modhelper.ModPackageManagerV2.onFileInstalled
import com.CHH2000day.navalcreed.modhelper.ModPackageManagerV2.postInstall
import com.CHH2000day.navalcreed.modhelper.ModPackageManagerV2.renameConflict
import com.CHH2000day.navalcreed.modhelper.ModPackageManagerV2.requestInstall
import com.CHH2000day.navalcreed.modhelper.ModPackageManagerV2.uninstall
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger
import okio.buffer
import okio.sink
import okio.source
import java.io.*

class LoginMovieReplacer : ModFragment() {
    private var srcFileUri: Uri? = null
    private lateinit var v: View
    private lateinit var fileTextView: TextView
    private var target: File? = null
    private var requiresTranscode = true
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.loginmoviereplacer_fragment, null)
        fileTextView = v.findViewById(R.id.loginmoviereplacerfragmentTextView)
        val select = v.findViewById<Button>(R.id.loginmoviereplacerfragmentButtonSelect)
        val update = v.findViewById<Button>(R.id.loginmoviereplacerfragmentButtonUpdate)
        val remove = v.findViewById<Button>(R.id.loginmoviereplacerfragmentButtonRemove)
        select.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, getText(R.string.select_a_file_selector)), QUERY_CODE)
        }
        update.setOnClickListener {
            if (srcFileUri == null) {
                Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val adb = AlertDialog.Builder(activity!!)
            adb.setTitle(R.string.please_wait)
                    .setMessage(R.string.transcode_writing)
                    .setCancelable(false)
            val ad = adb.create()
            ad.setCancelable(false)
            @SuppressLint("HandlerLeak") val h: Handler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    ad.dismiss()
                    when (msg.what) {
                        0 ->                             //无异常
                            Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show()
                        1 ->                             //操作出现异常
                            Snackbar.make(v, (msg.obj as Throwable).message!!, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            ad.show()
            object : Thread("LoginMovieIOThread") {
                override fun run() {
                    var hasCache = false
                    val cacheFile = File(context?.cacheDir, CACHE_FILE_NAME)
                    try {
                        if (requestInstall(MOD_NAME, ModPackageInfo.MODTYPE_OTHER, ModPackageInfo.SUBTYPE_EMPTY)) {
                            Utils.ensureFileParent(targetFile)
                            if (targetFile.exists()) {
                                renameConflict(FILENAME)
                            }
                            if (requiresTranscode) {
                                val path = Utils.resolveFilePath(srcFileUri, context)
                                if (path == null) {
                                    val source = getInStream(srcFileUri)?.source()
                                    Utils.ensureFileParent(cacheFile)
                                    val sink = cacheFile.sink().buffer()
                                    if (source != null) {
                                        sink.writeAll(source)
                                        source.close()
                                        sink.flush()
                                        sink.close()
                                        hasCache = true
                                    }
                                }
                                val srcFile = if (hasCache) {
                                    cacheFile
                                } else {
                                    File(path)
                                }
                                val result = FFmpeg.execute(arrayOf("-y", "-i", srcFile.absolutePath, "-an", "-vcodec", "theora", targetFile.absolutePath))
                                if (result != Config.RETURN_CODE_SUCCESS) {
                                    throw IOException("Transcode failed")
                                }
                            } else {
                                Utils.copyFile(getInStream(srcFileUri), targetFile)
                            }
                            onFileInstalled(FILENAME)
                            postInstall(-10)
                            h.sendEmptyMessage(0)
                        }
                    } catch (e: IOException) {
                        h.sendMessage(h.obtainMessage(1, e))
                        ModPackageManagerV2.onInstallFail()
                    } finally {
                        if (cacheFile.exists()) {
                            cacheFile.delete()
                        }
                    }
                }
            }.start()
        }
        remove.setOnClickListener { Snackbar.make(v, if (uninstall(MOD_NAME) == 0) R.string.success else R.string.failed, Snackbar.LENGTH_LONG).show() }
        return v
    }

    private val targetFile: File
        get() {
            if (target == null) {
                target = File(mainActivity.modHelperApplication.resFilesDir, FILENAME)
            }
            return target!!
        }

    private fun doLoad(uri: Uri?) {
        try {
            //OGG与OGV拥有相同的magic number
            srcFileUri = uri
            Logger.d("Get uri: authority:%s path:%s", srcFileUri!!.encodedAuthority, srcFileUri!!.encodedPath)
            requiresTranscode = Utils.FORMAT_OGG != Utils.identifyFormat(getInStream(uri), true)
        } catch (e: IOException) {
            srcFileUri = null
            Snackbar.make(v, R.string.failed, Snackbar.LENGTH_LONG).show()
        }
    }

    @Throws(FileNotFoundException::class)
    private fun getInStream(uri: Uri?): InputStream? {
        val inputStream: InputStream?
        val path = Utils.resolveFilePath(uri, context)
        inputStream = path?.let { FileInputStream(it) }
                ?: context!!.contentResolver.openInputStream(uri!!)
        return inputStream
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != QUERY_CODE) {
            return
        }
        if (resultCode != AppCompatActivity.RESULT_OK) {
            return
        }
        if (data == null || data.data == null) {
            Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show()
            return
        }
        doLoad(data.data)
        if (srcFileUri != null) {
            fileTextView.text = srcFileUri!!.path
        }
    }

    override fun uninstallMod(): Boolean {
        return false
    }

    companion object {
        private const val MOD_NAME = "CUSTOM_LOGINMOVIE"
        private const val FILENAME = "loginmovie.ogv"
        private const val QUERY_CODE = 2
        private const val CACHE_FILE_NAME = "video.cache"
    }
}