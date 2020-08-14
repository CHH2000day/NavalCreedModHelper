package com.CHH2000day.navalcreed.modhelper

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.DialogInterface.OnShowListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.CHH2000day.navalcreed.modhelper.CustomShipNameHelper.init
import com.CHH2000day.navalcreed.modhelper.ModPackageInstallerFragment.UriLoader
import com.CHH2000day.navalcreed.modhelper.ModPackageManagerV2.MigrationHelper
import com.chh2000day.navalcreedmodhelper_v2.structs.AnnouncementResult
import com.chh2000day.navalcreedmodhelper_v2.structs.ServerResult
import com.chh2000day.navalcreedmodhelper_v2.structs.VersionCheckResult
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody
import okio.Sink
import okio.buffer
import okio.sink
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.util.*

open class Main : AppCompatActivity(), UriLoader {
    private lateinit var mViewPager: ViewPager

    private var useAlphaChannel = BuildConfig.DEBUG
    private var updateApk: File? = null
    private lateinit var mContentView: ViewGroup
    private val json = Json(JsonConfiguration(ignoreUnknownKeys = true, allowStructuredMapKeys = true))

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val customShipnamePath = StringBuilder()
                .append(modHelperApplication.resFilesDirPath)
                .append(File.separatorChar)
                .append("datas")
                .append(File.separatorChar)
                .append("customnames.lua").toString()
        val customShipNameFile = File(customShipnamePath)
        if (!customShipNameFile.exists()) {
            Utils.ensureFileParent(customShipNameFile)
            try {
                customShipNameFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        init(customShipNameFile)
        modHelperApplication.reconfigModPackageManager()
        val oldConfigFile = File(modHelperApplication.resFilesDir, ModHelperApplication.STOREDFILE_NAME)
        if (oldConfigFile.exists()) {
            ModPackageManager.getInstance().init(this)
            try {
                ModPackageManager.getInstance().config(oldConfigFile)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        setContentView(R.layout.main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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
            GlobalScope.launch(Dispatchers.Main) {
                val adb = AlertDialog.Builder(this@Main)
                adb.setTitle(R.string.verifying_tester_authority)
                        .setMessage(R.string.please_wait)
                        .setCancelable(false)
                val ad = adb.create()
                ad.setCanceledOnTouchOutside(false)
                ad.show()
                when (val result = performStartTesterPermissionCheck()) {
                    keyCheckResult.KeyCheckSuccess -> {
                        withContext(Dispatchers.Main) {
                            ad.dismiss()
                        }
                    }
                    is keyCheckResult.KeyCheckFail -> {
                        Snackbar.make(mViewPager, result.msg, Snackbar.LENGTH_LONG).show()
                        showKeyDialog()
                    }
                }
            }

        }
    }

    sealed class keyCheckResult {
        object KeyCheckSuccess : keyCheckResult()
        data class KeyCheckFail(val msg: String) : keyCheckResult()
    }

    suspend fun doKeyCheck(key: String?): keyCheckResult {
        return withContext(Dispatchers.IO) {
            if (KeyUtil.checkKeyFormat(key)) {
                val builder = Request.Builder()
                val body: RequestBody = FormBody.Builder()
                        .add(ServerActions.ACTION, ServerActions.ACTION_CHECKTEST)
                        .add(ServerActions.VALUE_KEY, key!!)
                        .add(ServerActions.VALUE_SSAID, devId)
                        .add(ServerActions.VALUE_DEVICE, Build.MODEL)
                        .add(ServerActions.VALUE_LEGACY, "0")
                        .build()
                builder.url(ServerActions.REQUEST_URL)
                builder.post(body)
                val response = OKHttpHelper.getClient().newCall(builder.build()).execute()
                if (response.isSuccessful) {
                    try {
                        val resultStr = response.body?.source()?.readUtf8()
                        if (resultStr.isNullOrBlank()) {

                            return@withContext keyCheckResult.KeyCheckFail("Empty reply")
                        }
                        val bean = json.parse(ServerResult.serializer(), resultStr)
                        if (bean is ServerResult.Success) {
                            modHelperApplication.mainSharedPreferences.edit().putString(KEY_AUTHKEY, key).apply()
                            return@withContext keyCheckResult.KeyCheckSuccess
                        } else {
                            bean as ServerResult.Fail
                            return@withContext keyCheckResult.KeyCheckFail(bean.errorCode.name + " " + bean.message)
                        }
                    } catch (ignored: IllegalStateException) {
                    } finally {
                        response.close()
                    }
                } else {
                    return@withContext keyCheckResult.KeyCheckFail("Unknown")
                }
            }
            return@withContext keyCheckResult.KeyCheckFail("Unknown")
        }
    }

    private suspend fun performStartTesterPermissionCheck(): keyCheckResult {
        val key = modHelperApplication.mainSharedPreferences.getString(KEY_AUTHKEY, "")
        return doKeyCheck(key)
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

    override fun getUri_(): Uri? {
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

    protected inner class UpdateThread : Thread() {
        override fun run() {
            CoroutineScope(Dispatchers.IO).launch {
                //Get current version
                val currentVer = if (useAlphaChannel) BuildConfig.BuildVersion else packageManager.getPackageInfo(packageName, 0).versionCode
                //Generate request
                val body: RequestBody = FormBody.Builder()
                        .add(ServerActions.ACTION, ServerActions.ACTION_CHECKUPDATE)
                        .add(ServerActions.VALUE_BUILD_TYPE, if (useAlphaChannel) ServerActions.BUILD_TYPE_ALPHA else ServerActions.BUILD_TYPE_RELEASE)
                        .add(ServerActions.VALUE_LEGACY, "0")
                        .build()
                val builder = Request.Builder()
                builder.url(ServerActions.REQUEST_URL)
                        .post(body)
                val client = OKHttpHelper.getClient()
                //Send request
                val response = client.newCall(builder.build()).execute()
                if (response.isSuccessful) {
                    try {
                        val resultStr = response.body?.source()?.readUtf8() ?: return@launch
                        @Suppress("ConstantConditionIf")
                        when (val bean = json.parse(ServerResult.serializer(), resultStr)) {
                            is VersionCheckResult.Success -> {
                                val versionInfo = (if (BuildConfig.FLAVOR == TYPE_COMMON) bean.commonInfo else bean.ffmpegInfo)
                                        ?: return@launch
                                if (versionInfo.buildCode > currentVer) {

                                    val adb = AlertDialog.Builder(this@Main)
                                    adb.setTitle(R.string.update)
                                            .setMessage(versionInfo.changelog)
                                            .setPositiveButton(R.string.update) { _, _ ->
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    //Downloading
                                                    val alertDialogBuilder = AlertDialog.Builder(this@Main)
                                                    alertDialogBuilder.setCancelable(false)
                                                            .setTitle(R.string.please_wait)
                                                            .setMessage(R.string.downloading)
                                                    val ad = alertDialogBuilder.create()
                                                    ad.show()
                                                    withContext(Dispatchers.IO) {
                                                        val requestBuilder = Request.Builder()
                                                        requestBuilder.url(versionInfo.url)
                                                        val response = client.newCall(requestBuilder.build()).execute()
                                                        if (response.isSuccessful) {
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
                                                                withContext(Dispatchers.Main) {
                                                                    ad.dismiss()
                                                                    if (isOK) installApk()
                                                                }
                                                            }
                                                        } else {
                                                            withContext(Dispatchers.Main) {
                                                                ad.dismiss()
                                                                Snackbar.make(mContentView, R.string.failed, Snackbar.LENGTH_LONG).show()
                                                            }
                                                            Logger.e("Failed to download update")
                                                        }
                                                    }
                                                }
                                            }
                                            .setCancelable(false)
                                    //If update is not forced
                                    val isForceUpdate = currentVer < versionInfo.minVer
                                    if (!isForceUpdate) {
                                        adb.setNegativeButton(R.string.cancel, null)
                                        adb.setCancelable(true)
                                    }
                                    //Post dialog builder
                                    withContext(Dispatchers.Main) {
                                        val alertDialog = adb.create()
                                        if (isForceUpdate) {
                                            alertDialog.setCanceledOnTouchOutside(false)
                                            alertDialog.show()
                                        }
                                    }
                                }
                            }
                            is ServerResult.Fail -> {
                                Logger.d("Failed to check update:${bean.errorCode}  ${bean.message}")
                            }
                            else -> {
                                Logger.w("Unexpected case")
                            }
                        }
                    } catch (ignored: IllegalStateException) {
                    } finally {
                        response.close()
                    }
                } else {
                    //Failed to connect to server
                    Logger.w("Failed to get update data.${response.message}")
                }
            }
        }
    }

    private inner class AnnouncementThread : Thread() {
        override fun run() {
            super.run()
            CoroutineScope(Dispatchers.IO).launch {
                val client = OKHttpHelper.getClient()
                val builder = Request.Builder()
                val formBuilder = FormBody.Builder()
                formBuilder.add(ServerActions.ACTION, ServerActions.ACTION_GET_ANNOUNCEMENT)
                formBuilder.add(ServerActions.VALUE_BUILD_TYPE, if (BuildConfig.DEBUG) ServerActions.BUILD_TYPE_ALPHA else ServerActions.BUILD_TYPE_RELEASE)
                formBuilder.add(ServerActions.VALUE_LEGACY, "0")
                builder.url(ServerActions.REQUEST_URL)
                builder.post(formBuilder.build())
                val response = client.newCall(builder.build()).execute()
                if (response.isSuccessful) {
                    try {
                        val resultStr = response.body?.source()?.readUtf8() ?: return@launch
                        val bean = json.parse(ServerResult.serializer(), resultStr)
                        if (bean is AnnouncementResult.Success) {
                            val id = bean.id
                            val localAnnouncementId = getSharedPreferences(GENERAL, 0).getInt(ANNOU_VER, -1)
                            val adb = AlertDialog.Builder(this@Main)
                            if (id > localAnnouncementId) {
                                adb.setTitle(bean.title)
                                        .setMessage(bean.announcement)
                                        .setPositiveButton(R.string.ok, null)
                                        .setNeutralButton(R.string.dont_show) { _: DialogInterface?, _: Int -> getSharedPreferences(GENERAL, 0).edit().putInt(ANNOU_VER, id).apply() }
                                        .setNegativeButton(R.string.copy) { _: DialogInterface?, _: Int ->
                                            val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            getSharedPreferences(GENERAL, 0).edit().putInt(ANNOU_VER, id).apply()
                                            if (!TextUtils.isEmpty(bean.toCopy)) {
                                                cmb.setPrimaryClip(ClipData.newPlainText("label", bean.toCopy.trim()))
                                            }
                                        }
                                withContext(Dispatchers.Main) {
                                    adb.show()
                                }
                            }
                        } else if (bean is ServerResult.Fail) {
                            Logger.d("Failed to get Announcement:${bean.errorCode}  ${bean.message}")
                        }
                    } catch (ignored: IllegalStateException) {
                    } finally {
                        response.close()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(mContentView, R.string.failed, Snackbar.LENGTH_LONG).show()
                        Logger.e("Failed to get announcement.${response.message}")
                    }
                }
            }
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
                GlobalScope.launch(Dispatchers.Main) {
                    val key = keyinput.editableText.toString().toUpperCase(Locale.ROOT).trim()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(keyinput.windowToken, 0)
                    when (val result = doKeyCheck(key)) {
                        keyCheckResult.KeyCheckSuccess -> {
                            ad.dismiss()
                        }
                        is keyCheckResult.KeyCheckFail -> {
                            Snackbar.make(mViewPager, result.msg, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
                //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            })
            keyinput.editableText.append(modHelperApplication.mainSharedPreferences.getString(KEY_AUTHKEY, ""))
            btnEnter.setOnLongClickListener(OnLongClickListener {
                val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cmb.setPrimaryClip(ClipData.newPlainText("SSAID", devId))
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