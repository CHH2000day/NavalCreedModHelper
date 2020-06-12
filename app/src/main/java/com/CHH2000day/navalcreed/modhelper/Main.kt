package com.CHH2000day.navalcreed.modhelper

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.CHH2000day.navalcreed.modhelper.GsonHelper.gson
import com.CHH2000day.navalcreed.modhelper.ModPackageInstallerFragment.UriLoader
import com.CHH2000day.navalcreed.modhelper.ModPackageManagerV2.MigrationHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import okhttp3.*
import okio.Sink
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.util.*

open class Main : AppCompatActivity(), UriLoader {
    private lateinit var mViewPager: ViewPager
    private lateinit var mUpdateHandler: Handler
    private lateinit var mAlphaCheckHandler: Handler

    private var useAlphaChannel = BuildConfig.DEBUG
    private var updateApk: File? = null
    private lateinit var mContentView: ViewGroup

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        mUpdateHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val adb = msg.obj as AlertDialog.Builder
                val ad = adb.create()
                ad.setCanceledOnTouchOutside(false)
                ad.show()
            }
        }
        //配置ViewPager与TabLayout
        mContentView = findViewById(R.id.maincontentview)
        mViewPager = findViewById(R.id.viewPager)
        val mTabLayout = findViewById<TabLayout>(R.id.tabLayout)
        //构造Fragment实例
        val mBGReplacerFragment = BGReplacerFragment()
        val mLoginMovieReplacer = LoginMovieReplacer()
        val mCrewPicReplacerFragment = CrewPicReplacerFragment()
        val mCustomShipNameFragment = CustomShipNameFragment()
        val mBGMReplacerFragment = BGMReplacerFragment()
        val mModPkgInstallerFragment = ModPackageInstallerFragment()
        val mModPackageManagerFragment = ModPackageManagerFragmentV2()
        //进行数据配置
        val fragments: MutableList<Fragment> = ArrayList()
        fragments.add(mBGReplacerFragment)
        fragments.add(mLoginMovieReplacer)
        fragments.add(mCrewPicReplacerFragment)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (resources.configuration.locales[0].language.contains("zh")) {
                fragments.add(mCustomShipNameFragment)
            }
        } else {
            @Suppress("DEPRECATION")
            if (resources.configuration.locale.language.contains("zh")) {
                fragments.add(mCustomShipNameFragment)
            }
        }

        fragments.add(mBGMReplacerFragment)
        fragments.add(mModPkgInstallerFragment)
        fragments.add(mModPackageManagerFragment)
        fragments.add(AboutFragment())
        val fragmentTitles = resources.getStringArray(R.array.fragment_titles)
        val titles: List<String> = ArrayList(listOf(*fragmentTitles))
        val mAdapter: FragmentPagerAdapter = ViewPagerAdapter(supportFragmentManager, fragments, titles)
        mViewPager.adapter = mAdapter
        mTabLayout.setupWithViewPager(mViewPager)
        checkValidity()
        UpdateThread().start()
        AnnouncementThread().start()
        if (Intent.ACTION_VIEW == intent.action) {
            mTabLayout.getTabAt(fragments.indexOf(mModPkgInstallerFragment))!!.select()
        }
    }

    override fun onStart() {
        super.onStart()
        if (ModPackageManager.getInstance().inited) {
            MigrationHelper(this).execute(modHelperApplication.oldConfigFile)
            ModPackageManager.getInstance().inited = false
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    @SuppressLint("HandlerLeak")
    private fun checkValidity() {
        //Perform check
        val key = (application as ModHelperApplication).mainSharedPreferences.getString(KEY_AUTHKEY, "")
        if (BuildConfig.DEBUG || !TextUtils.isEmpty(key) && KeyUtil.checkKeyFormat(key)) {
            //If a test key is found,disable ad
            useAlphaChannel = modHelperApplication.mainSharedPreferences.getBoolean(KEY_USEALPHACHANNEL, BuildConfig.DEBUG)
        }
        if (BuildConfig.DEBUG) {
            val adb = AlertDialog.Builder(this@Main)
            adb.setTitle(R.string.verifying_tester_authority)
                    .setMessage(R.string.please_wait)
                    .setCancelable(false)
            val ad = adb.create()
            ad.setCanceledOnTouchOutside(false)
            mAlphaCheckHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        9010 -> {
                            Snackbar.make(mViewPager, R.string.network_err, Snackbar.LENGTH_LONG).show()
                            ad.setButton(DialogInterface.BUTTON_POSITIVE, getText(R.string.exit)) { _: DialogInterface?, _: Int -> doExit() }
                        }
                        else -> {
                            Snackbar.make(mViewPager, R.string.failed_to_check_tester_authority, Snackbar.LENGTH_LONG).show()
                            ad.dismiss()
                            showKeyDialog()
                        }
                    }
                }
            }
            ad.show()
            performStartTesterPermissionCheck(object : OnCheckResultListener {
                override fun onSuccess() {
                    ad.dismiss()
                }

                override fun onFail(reason: Int, errorMsg: String?) {
                    if (reason == 0) {
                        //如果设备不匹配，清除本地许可数据
                        (application as ModHelperApplication).mainSharedPreferences.edit().putString(KEY_OBJID, "").putString(KEY_AUTHKEY, "").apply()
                    }
                    mAlphaCheckHandler.sendEmptyMessage(reason)
                }
            })
        }
    }

    fun doKeyCheck(key: String?, listener: OnCheckResultListener) {
        if (KeyUtil.checkKeyFormat(key)) {
            val builder = Request.Builder()
            val body: RequestBody = FormBody.Builder()
                    .add(ServerActions.ACTION, ServerActions.ACTION_CHECKTEST)
                    .add(ServerActions.VALUE_KEY, key!!)
                    .add(ServerActions.VALUE_SSAID, devId)
                    .add(ServerActions.VALUE_DEVICE, Build.MODEL)
                    .build()
            builder.url(ServerActions.REQUEST_URL)
            builder.post(body)
            OKHttpHelper.getClient().newCall(builder.build()).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    listener.onFail(1, "Failed to connect to server")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val bean = Gson().fromJson(response.body!!.charStream(), DataBean::class.java)
                        if (bean.getResultCode() >= 0) {
                            if (bean.getResultCode() > 0) Logger.d(bean.getMessage())
                            modHelperApplication.mainSharedPreferences.edit().putString(KEY_AUTHKEY, key).apply()
                            listener.onSuccess()
                        } else {
                            listener.onFail(bean.getResultCode(), bean.getMessage())
                        }
                    } catch (ignored: IllegalStateException) {
                    } finally {
                        response.close()
                    }
                }
            })
        } else {
            //密钥格式验证失败
            listener.onFail(0, "Invalid Key")
            return
        }
    }

    private fun performStartTesterPermissionCheck(listener: OnCheckResultListener) {
        val key = modHelperApplication.mainSharedPreferences.getString(KEY_AUTHKEY, "")
        if (!KeyUtil.checkKeyFormat(key)) {
            listener.onFail(2, "Invalid local key!")
            return
        }
        val builder = Request.Builder()
        val body: RequestBody = FormBody.Builder()
                .add(ServerActions.ACTION, ServerActions.ACTION_CHECKTEST)
                .add(ServerActions.VALUE_KEY, key!!)
                .add(ServerActions.VALUE_SSAID, devId)
                .add(ServerActions.VALUE_DEVICE, Build.MODEL)
                .build()
        builder.url(ServerActions.REQUEST_URL)
        builder.post(body)
        OKHttpHelper.getClient().newCall(builder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onFail(1, "Failed to connect to server")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val bean = Gson().fromJson(response.body!!.charStream(), DataBean::class.java)
                    if (bean.getResultCode() >= 0) {
                        listener.onSuccess()
                        if (bean.getResultCode() > 0) Logger.d(bean.getMessage())
                    } else {
                        listener.onFail(bean.getResultCode(), bean.getMessage())
                        Logger.w(bean.getMessage())
                    }
                } catch (ignored: IllegalStateException) {
                    listener.onFail(-20, "Invalid response")
                } finally {
                    response.close()
                }
            }
        })
    }

    //return Build.SERIAL;
    val devId: String
        @SuppressLint("HardwareIds")
        get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    //return Build.SERIAL;

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val adb = AlertDialog.Builder(this)
                adb.setTitle(R.string.permission_request)
                        .setMessage(R.string.permission_request_msg)
                        .setNegativeButton(R.string.cancel_and_exit) { _: DialogInterface?, _: Int -> finish() }
                        .setPositiveButton(R.string.grant_permission) { _: DialogInterface?, _: Int -> ActivityCompat.requestPermissions(this@Main, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_CHECK_CODE) }
                        .setCancelable(false)
                val ad = adb.create()
                ad.setCanceledOnTouchOutside(false)
                ad.show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PERMISSION_CHECK_CODE == requestCode) {
            if (grantResults.isEmpty() || PackageManager.PERMISSION_GRANTED != grantResults[0]) {
                checkPermission()
            } else {
                (application as ModHelperApplication).reconfigModPackageManager()
            }
        }
        if (requestCode == REQUEST_CODE_APP_INSTALL && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            installApk()
        }
    }

    val modHelperApplication: ModHelperApplication
        get() = application as ModHelperApplication

    override fun onBackPressed() {
        if (!(application as ModHelperApplication).isMainPage) {
            super.onBackPressed()
        } else {
            exit()
        }
    }

    private fun exit() {
        val adb = AlertDialog.Builder(this)
        adb.setTitle(R.string.notice)
                .setMessage(R.string.exitmsg)
                .setPositiveButton(R.string.exit) { _: DialogInterface?, _: Int -> doExit() }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    private fun doExit() {
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_exit) {
            exit()
        }
        return true
    }

    fun isUseAlphaChannel(): Boolean {
        return useAlphaChannel
    }

    fun setUseAlphaChannel(useAlphaChannel: Boolean) {
        this.useAlphaChannel = useAlphaChannel
        modHelperApplication.mainSharedPreferences.edit().putBoolean(KEY_USEALPHACHANNEL, this.useAlphaChannel).apply()
    }

    override fun getUri(): Uri? {
        return if (Intent.ACTION_VIEW == intent.action) {
            intent.data!!
        } else null
    }

    fun showKeyDialog() {
        val d = layoutInflater.inflate(R.layout.dialog_key, null)
        val et = d.findViewById<EditText>(R.id.dialogkeyEditTextKey)
        val adb = AlertDialog.Builder(this)
        adb.setTitle(R.string.tester_authority_verify)
                .setView(d)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.exit, null)
                .setCancelable(false)
        val ad = adb.create()
        val listener = KeyDialogListener(ad, et)
        ad.setOnShowListener(listener)
        ad.show()
    }

    private fun installApk() {
        if (updateApk != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    startActivityForResult(intent, REQUEST_CODE_APP_INSTALL)
                }
            }
            val i = Intent(Intent.ACTION_VIEW)
            val data: Uri
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(this@Main, "com.CHH2000day.navalcreed.modhelper.fileprovider", updateApk!!)
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                data = Uri.fromFile(updateApk)
            }
            i.setDataAndType(data, "application/vnd.android.package-archive")
            startActivity(i)
            updateApk = null
        }
    }

    interface OnCheckResultListener {
        fun onSuccess()
        fun onFail(reason: Int, errorMsg: String?)
    }

    protected inner class UpdateThread : Thread() {
        override fun run() {
            try {
                //Get current version
                val currentVer = if (useAlphaChannel) BuildConfig.BuildVersion else packageManager.getPackageInfo(packageName, 0).versionCode
                //Generate request
                val body: RequestBody = FormBody.Builder()
                        .add(ServerActions.ACTION, ServerActions.ACTION_CHECKUPDATE)
                        .add(ServerActions.VALUE_BUILD_TYPE, if (useAlphaChannel) ServerActions.BUILD_TYPE_ALPHA else ServerActions.BUILD_TYPE_RELEASE)
                        .build()
                val builder = Request.Builder()
                builder.url(ServerActions.REQUEST_URL)
                        .post(body)
                val client = OKHttpHelper.getClient()
                //Send request
                client.newCall(builder.build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        //Failed to connect to server
                        Logger.w("Failed to get update data.")
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        try {
                            val bean = gson.fromJson(response.body!!.charStream(), VersionBean::class.java)
                            @Suppress("ConstantConditionIf")
                            if (bean.resultCode >= 0) {
                                //Handle normal situation
                                //Type is either ffmpeg or common
                                val versionInfo = if (BuildConfig.FLAVOR == TYPE_COMMON) bean.commonInfo else bean.ffmpegInfo
                                //If any update is available
                                if (versionInfo.buildCode > currentVer) {
                                    val adb = AlertDialog.Builder(this@Main)
                                    adb.setTitle(R.string.update)
                                            .setMessage(versionInfo.changelog)
                                            .setPositiveButton(R.string.update) { _, _ ->
                                                //Downloading
                                                val alertDialogBuilder = AlertDialog.Builder(this@Main)
                                                alertDialogBuilder.setCancelable(false)
                                                        .setTitle(R.string.please_wait)
                                                        .setMessage(R.string.downloading)
                                                val ad = alertDialogBuilder.create()
                                                ad.show()
                                                object : Thread() {
                                                    override fun run() {
                                                        super.run()
                                                        //Open connection
                                                        val requestBuilder = Request.Builder()
                                                        requestBuilder.url(versionInfo.url)
                                                        client.newCall(requestBuilder.build()).enqueue(object : Callback {
                                                            override fun onFailure(call: Call, e: IOException) {
                                                                ad.dismiss()
                                                                Snackbar.make(mContentView, R.string.failed, Snackbar.LENGTH_LONG).show()
                                                                Logger.e(e, "FAiled to download update")
                                                            }

                                                            @Throws(IOException::class)
                                                            override fun onResponse(call: Call, response: Response) {
                                                                var isOK = false
                                                                try {
                                                                    //Ensure target file is accessible
                                                                    val f = File(externalCacheDir, "update.apk")
                                                                    Utils.ensureFileParent(f)
                                                                    val sink: Sink = f.sink()
                                                                    //Write to file
                                                                    val bufferedSink = sink.buffer()
                                                                    bufferedSink.writeAll(response.body!!.source())
                                                                    bufferedSink.flush()
                                                                    bufferedSink.close()
                                                                    sink.close()
                                                                    updateApk = f
                                                                    isOK = true
                                                                } catch (e: Exception) {
                                                                    e.printStackTrace()
                                                                    Logger.e(e, "Error while downloading")
                                                                } finally {
                                                                    ad.dismiss()
                                                                    if (isOK) installApk()
                                                                }
                                                            }
                                                        })
                                                    }
                                                }.start()
                                            }
                                            .setCancelable(false)
                                    //If update is not forced
                                    if (currentVer >= versionInfo.minVer) {
                                        adb.setNegativeButton(R.string.cancel, null)
                                        adb.setCancelable(true)
                                        mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(0, adb))
                                    }
                                    //Post dialog builder
                                }
                            } else {
                                Logger.d("Server return result code")
                                //Server returns an error
                            }
                        } catch (ignored: IllegalStateException) {
                        } finally {
                            response.close()
                        }
                    }
                })
            } catch (e: Exception) {
                Logger.e(e, e.localizedMessage)
            }
        }
    }

    private inner class AnnouncementThread : Thread() {
        override fun run() {
            super.run()
            val client = OKHttpHelper.getClient()
            val builder = Request.Builder()
            val formBuilder = FormBody.Builder()
            formBuilder.add(ServerActions.ACTION, ServerActions.ACTION_GET_ANNOUNCEMENT)
            formBuilder.add(ServerActions.VALUE_BUILD_TYPE, if (BuildConfig.DEBUG) ServerActions.BUILD_TYPE_ALPHA else ServerActions.BUILD_TYPE_RELEASE)
            builder.url(ServerActions.REQUEST_URL)
            builder.post(formBuilder.build())
            client.newCall(builder.build()).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Snackbar.make(mContentView, R.string.failed, Snackbar.LENGTH_LONG).show()
                    Logger.e(e, "Failed to get announcement")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val bean = gson.fromJson(response.body!!.charStream(), AnnouncementBean::class.java)
                        if (bean.getResultCode() >= 0) {
                            val id = bean.id
                            val localAnnouncementId = getSharedPreferences(GENERAL, 0).getInt(ANNOU_VER, -1)
                            val adb0 = AlertDialog.Builder(this@Main)
                            if (id > localAnnouncementId) {
                                adb0.setTitle(bean.title)
                                        .setMessage(bean.announcement)
                                        .setPositiveButton(R.string.ok, null)
                                        .setNeutralButton(R.string.dont_show) { _: DialogInterface?, _: Int -> getSharedPreferences(GENERAL, 0).edit().putInt(ANNOU_VER, id).apply() }
                                        .setNegativeButton(R.string.copy) { _: DialogInterface?, _: Int ->
                                            val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            getSharedPreferences(GENERAL, 0).edit().putInt(ANNOU_VER, id).apply()
                                            if (!TextUtils.isEmpty(bean.toCopy)) {
                                                cmb.text = bean.toCopy.trim()
                                            }
                                        }
                                mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(1, adb0))
                            }
                        } else {
                            Snackbar.make(mContentView, bean.getMessage(), Snackbar.LENGTH_LONG).show()
                        }
                    } catch (ignored: IllegalStateException) {
                    } finally {
                        response.close()
                    }
                }
            })
        }
    }

    private inner class KeyDialogListener(private val ad: AlertDialog, private val keyinput: EditText) : OnShowListener {
        private lateinit var btnCancel: Button
        private lateinit var btnEnter: Button
        override fun onShow(p1: DialogInterface) {
            btnCancel = ad.getButton(DialogInterface.BUTTON_NEGATIVE)
            btnEnter = ad.getButton(DialogInterface.BUTTON_POSITIVE)
            btnCancel.setOnClickListener(View.OnClickListener { doExit() })
            btnEnter.setOnClickListener(View.OnClickListener {
                val key = keyinput.editableText.toString().toUpperCase(Locale.ROOT).trim()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(keyinput.windowToken, 0)
                //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                doKeyCheck(key, object : OnCheckResultListener {
                    override fun onSuccess() {
                        ad.dismiss()
                    }

                    override fun onFail(reason: Int, errorMsg: String?) {
                        Snackbar.make(mViewPager, errorMsg!!, Snackbar.LENGTH_LONG).show()
                    }
                })
            })
            keyinput.editableText.append(modHelperApplication.mainSharedPreferences.getString(KEY_AUTHKEY, ""))
            btnEnter.setOnLongClickListener(OnLongClickListener {
                val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cmb.text = devId
                true
            })
        }

    }

    companion object {
        const val KEY_AUTHKEY = "authKey"
        private const val GENERAL = "general"
        private const val ANNOU_VER = "annover"
        private const val KEY_OBJID = "objID"
        private const val KEY_USEALPHACHANNEL = "useAlphaCh"
        private const val PERMISSION_CHECK_CODE = 125
        private const val REQUEST_CODE_APP_INSTALL = 126
        private const val TYPE_COMMON = "common"
        private const val TYPE_FFMPEG = "ffmpeg"
    }
}