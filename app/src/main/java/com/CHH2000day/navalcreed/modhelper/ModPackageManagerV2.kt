package com.CHH2000day.navalcreed.modhelper

import android.app.ProgressDialog
import android.os.AsyncTask
import androidx.annotation.NonNull
import com.orhanobut.logger.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import kotlin.concurrent.thread

object ModPackageManagerV2 {
    var override = false

    private const val managerVer = 2
    private lateinit var dataFile: File
    private var inited = false

    private var modList = mutableListOf<ModInstallationInfo>()
    private var pendingTask: PendingInstallation? = null
    private var installationFiles: MutableSet<String> = mutableSetOf()
    private const val CONFLICT_SUFFIX = ".old"
    private var duplicatedFileInfo: MutableSet<DuplicationInfo> = mutableSetOf()
    private lateinit var application: ModHelperApplication
    private val installConflictFiles = mutableSetOf<String>()
    private var onDataChangedListener: OnDataChangedListener? = null
    private var modType = mutableMapOf<String, String>()
    private val json = Json(JsonConfiguration(ignoreUnknownKeys = true))
    fun config(file: File, application: ModHelperApplication): Boolean {
        dataFile = file
        this.application = application
        init(application)
        Logger.d("ModPackageManagerV2 configured")
        return true
    }

    fun registerOnDataChangeListener(listener: OnDataChangedListener) {
        onDataChangedListener = listener
    }

    fun unregisterOnDataChangeListener() {
        onDataChangedListener = null
    }

    /**
     * Check if there's a conflict with existing mod
     */
    fun checkInstall(name: String, type: String, subType: String, version: Int, files: MutableSet<String>): QueryResult {
        Logger.d("Checking installation status for mod $name-$type-$subType version$version")
        val result = QueryResult(result = QueryResult.RESULT_OK, conflictList = mutableSetOf())
        if (override) {
            return result
        }
        for (mod in modList) {
            lateinit var resultSet: Set<String>
            if (mod.type == type) {
                if (mod.name == name && mod.version <= version) {
                    result.result = QueryResult.RESULT_UPDATE
                    Logger.d("Result:update")
                    return result
                }
                if ((type != ModPackageInfo.MODTYPE_CV) || (type == ModPackageInfo.MODTYPE_CV && subType == mod.subType)) {
                    resultSet = mod.files.filter {
                        files.contains(it)
                    }.toSet()
                    if (resultSet.isNotEmpty()) {
                        result.result = QueryResult.RESULT_CONFLICT
                        result.conflictList = resultSet
                        Logger.d("Result:conflict")
                        return result
                    }
                }
            }
        }
        Logger.d("Result:OK")
        return result
    }

    @Synchronized
    fun uninstall(name: String): Int {
        Logger.d("Start to uninstall mod:$name")
        val installation = getInstallation(name) ?: return -10
        installation.files.toList().forEach {
            recoverFileFromConflict(it, installation)
        }
        modList.remove(installation)
        //Cleanup remaining mod info caused by previous version
        if (modList.isEmpty()) {
            duplicatedFileInfo.forEach { info ->
                info.files.forEach { fileInfo ->
                    File(fileInfo.currFileName).also {
                        if (it.exists()) {
                            it.delete()
                        }
                    }
                }
            }
            duplicatedFileInfo.clear()
        }
        refresh()
        Logger.d("Mod uninstall complete!")
        return 0
    }

    fun refresh() {
        if (duplicatedFileInfo.isNotEmpty()) {
            duplicatedFileInfo.filter {
                (it.files.size <= 2)
            }.forEach {
                Logger.d("Remove duplication info:${it.fileName}")
                duplicatedFileInfo.remove(it)
            }
        }
        onDataChangedListener?.onChange()
        writeConfig()
    }

    private fun recoverFileFromConflict(fileName: String, installation: ModInstallationInfo) {
        val basePath = getBasePath(installation)
        val rawName = fileName.replace(CONFLICT_SUFFIX, "")
        Logger.d("Remove conflict for file:$rawName")
        if (duplicatedFileInfo.isNotEmpty()) {
            duplicatedFileInfo.toList().forEach { info ->
                if (info.fileName == rawName) {
                    info.files.toList().forEach { f ->
                        if (f.currFileName == fileName) {
                            //Delete current file
                            val file = File(basePath, f.currFileName)
                            file.delete()
                            Logger.d("Deleted file:${file.path}")
                            info.files.remove(f)
                        }
                    }
                    //Remove a suffix for all elements before this effected
                    info.files.toList().forEach {
                        File(basePath, it.currFileName).renameTo(File(basePath, it.currFileName.removeSuffix(CONFLICT_SUFFIX)))
                        //Update installation record(1/2)
                        getInstallation(it.modName)?.files?.remove(it.currFileName)
                        //Update conflict record
                        it.currFileName = it.currFileName.removeSuffix(CONFLICT_SUFFIX)
                        //Update installation record(2/2)
                        getInstallation(it.modName)?.files?.add(it.currFileName)
                        //Remove info if only one element is left in the list
                        if (info.files.size <= 1) {
                            Logger.d("Setting status of mod \'${it.modName}\' to INSTALLED")
                            duplicatedFileInfo.remove(info)
                            getInstallation(it.modName)?.status = Status.INSTALLED
                        }
                    }
                    return
                }
            }

        }
        //If no duplication
        val f = File(basePath, fileName)
        f.delete()
        Logger.d("Deleted file:${f.path}")
    }

    /**
     * Use for rollback ONLY
     */
    private fun recoverFileFromConflict(fileName: String) {
        val basePath = getBasePath()
        for (info in duplicatedFileInfo) {
            if (info.fileName == fileName) {
                for (fileInfo in info.files.asReversed()) {
                    if (fileInfo.currFileName.endsWith(CONFLICT_SUFFIX)) {
                        File(basePath, fileInfo.currFileName).renameTo(File(basePath, fileInfo.currFileName.removeSuffix(CONFLICT_SUFFIX)))
                        //Update installation record(1/2)
                        getInstallation(fileInfo.modName)?.files?.remove(fileInfo.currFileName)
                        //Update conflict record
                        fileInfo.currFileName = fileInfo.currFileName.removeSuffix(CONFLICT_SUFFIX)
                        //Update installation record(2/2)
                        getInstallation(fileInfo.modName)?.files?.add(fileInfo.currFileName)
                        if (info.files.size <= 1) {
                            getInstallation(info.files[0].modName)?.status = Status.INSTALLED
                        }
                    } else {
                        //remove file as it's the last item in list.
                        //Modify installation info to partly working
                        File(basePath, fileInfo.currFileName).delete()
                        info.files.remove(fileInfo)
                    }
                }
                //End this method
                return
            }
        }
        //If no duplication
        File(basePath, fileName).delete()
    }

    @Synchronized
    fun renameConflict(name: String) {
        if (override) {
            return
        }
        Logger.d("Prepare to rename conflict file $name")
        installConflictFiles.add(name)
        for (info in duplicatedFileInfo) {
            if (info.fileName == name) {
                doRenameConflict(name, info)
                return
            }
        }
        doRenameConflict(name)
    }

    private fun doRenameConflict(fileName: String) {
        val duplicationInfo = DuplicationInfo(fileName)
        marker@ for (mod in modList) {
            if (pendingTask?.type == ModPackageInfo.MODTYPE_OTHER || (mod.type == pendingTask?.type && mod.subType == pendingTask?.subType))
                for (file in mod.files) {
                    if (fileName == file && mod.name != pendingTask?.name) {
                        duplicationInfo.files.add(DuplicatedFile(mod.name, fileName))
                        break@marker
                    }
                }
        }
        duplicatedFileInfo.add(duplicationInfo)
        if (duplicationInfo.files.isNotEmpty()) {
            duplicationInfo.files.add(DuplicatedFile(pendingTask!!.name, fileName))
            doRenameConflict(fileName, duplicationInfo)
        }

    }

    private fun doRenameConflict(fileName: String, info: DuplicationInfo) {
        for (duplicatedFile in info.files) {
            //Get installation info of existed mod,return if has null
            val installation = getInstallation(duplicatedFile.modName) ?: return
            //Not keep this if it's a update
            if (installation.name == pendingTask?.name) {
                installConflictFiles.remove(fileName)
                Logger.d("File :$fileName is a update,not add it to list")
                return
            }
            if ((installation.type == ModPackageInfo.MODTYPE_OTHER || (installation.type == pendingTask?.type && installation.subType == pendingTask?.subType))) {
                //Get base path
                val basePath = getBasePath(installation)
                //Rename it to $OLDNAME.old
                Logger.d("Renaming file for affected mod:${installation.name}")
                File(basePath, duplicatedFile.currFileName).renameTo(File(basePath, duplicatedFile.currFileName + CONFLICT_SUFFIX))
                //Update name in existed installation info
                installation.files.remove(duplicatedFile.currFileName)
                duplicatedFile.currFileName += CONFLICT_SUFFIX
                installation.files.add(duplicatedFile.currFileName)
                //Set status to partly working
                installation.status = Status.PARTLY_WORKING
                Logger.d("Mod:${installation.name} is PARTLY_WORKING")
            }
        }
        info.files.add(DuplicatedFile(pendingTask!!.name, fileName))
    }

    private fun getBasePath(installation: ModInstallationInfo): String {
        return ModPackageInstallHelper.getPath(installation.type, ModPackageInstallHelper.getSubTypeId(installation.subType), application)
    }

    private fun getBasePath(type: String, subType: String): String {
        return ModPackageInstallHelper.getPath(type, ModPackageInstallHelper.getSubTypeId(subType), application)
    }

    private fun getBasePath(): String {
        return getBasePath(pendingTask!!.type, pendingTask!!.subType)
    }

    fun getMods(): List<ModInstallationInfo> = modList.toList()

    fun getInstallation(name: String): ModInstallationInfo? {
        for (mod in modList) {
            if (mod.name == name) {
                return mod
            }
        }
        return null
    }

    fun getInstallation(type: String, subType: String): ModInstallationInfo? {
        for (mod in modList) {
            if (mod.type == type) {
                if (mod.type != ModPackageInfo.MODTYPE_CV || (type == ModPackageInfo.MODTYPE_CV && subType == mod.subType)) {
                    return mod
                }
            }
        }
        return null
    }

    /**
     * Mark for an installation running
     * Returns true if successfully requested
     * Returns false if another installation is being done
     */
    @Synchronized
    fun requestInstall(name: String, type: String, subType: String): Boolean {
        Logger.d("Requesting mod install:$name")
        if (pendingTask == null) {
            var isUpdate = false
            for (mod in modList) {
                if (mod.name == name && mod.type == type && mod.subType == subType) {
                    isUpdate = true
                    mod.status = Status.INSTALLING
                }
            }
            pendingTask = PendingInstallation(name = name, type = type, subType = subType, isUpdate = isUpdate)
            installationFiles = mutableSetOf()
            return true
        }
        return false
    }

    /**
     * Call this fun when a file is extracted.
     */
    fun onFileInstalled(name: String) {
        installationFiles.add(name)
    }

    /**
     * Call this once installation is done
     */
    fun postInstall(version: Int) {
        Logger.d("Mod installation completed")
        if (pendingTask != null) {
            if (pendingTask!!.isUpdate) {
                val installation: ModInstallationInfo? = getInstallation(pendingTask!!.name)
                installation?.status = Status.INSTALLED
                installation?.files?.plusAssign(installationFiles)
            } else {
                val installationInfo = ModInstallationInfo(name = pendingTask!!.name, type = pendingTask!!.type, subType = pendingTask!!.subType, version = version, status = Status.INSTALLED, files = installationFiles.toMutableSet())
                modList.add(installationInfo)
            }
        }
        pendingTask = null
        installationFiles.clear()
        refresh()
    }

    fun onInstallFail() {
        pendingTask = null
        refresh()
    }

    @Synchronized
    fun rollback() {
        Logger.i("Rolling back current installation")
        for (filename in installConflictFiles) {
            recoverFileFromConflict(filename)
        }
        val basePath = getBasePath()
        for (filename in installationFiles) {
            File(basePath, filename).delete()
        }
    }

    private fun init(application: ModHelperApplication) {
        try {
            if (dataFile.exists()) {
                val source = dataFile.source().buffer()
                val dataStr = source.readUtf8()
                val config = json.parse(Config.serializer(), dataStr)
                override = config.isOverride
                modList = config.modInfos
                duplicatedFileInfo = config.duplicationInfos
                source.close()
            }
        } catch (e: Exception) {
            Logger.d("Failed to load config")
        }

        inited = true
        val res = application.resources
        modType[ModPackageInfo.MODTYPE_OTHER] = res.getString(R.string.unknown)
        modType[ModPackageInfo.MODTYPE_BACKGROUND] = res.getString(R.string.modtype_background)
        modType[ModPackageInfo.MODTYPE_BGM] = res.getString(R.string.modtype_backgroundmusic)
        modType[ModPackageInfo.MODTYPE_CREWPIC] = res.getString(R.string.modtype_crewpic)
        modType[ModPackageInfo.MODTYPE_SOUNDEFFECT] = res.getString(R.string.modtype_soundeffect)
        modType[ModPackageInfo.MODTYPE_SOUNDEFFECT_PRIM] = res.getString(R.string.modtype_soundeffect_prim)
        modType[ModPackageInfo.MODTYPE_SOUNDEFFECT_SEC] = res.getString(R.string.modtype_soundeffect_sec)
        modType[ModPackageInfo.MODTYPE_CV] = res.getString(R.string.modtype_captainvoice)
        modType[ModPackageInfo.SUB_MODTYPE_CV_CN] = res.getString(R.string.modtype_captainvoice_cn)
        modType[ModPackageInfo.SUB_MODTYPE_CV_EN] = res.getString(R.string.modtype_captainvoice_en)
        modType[ModPackageInfo.SUB_MODTYPE_CV_JP_CV] = res.getString(R.string.modtype_captainvoice_ja_cv)
        modType[ModPackageInfo.SUB_MODTYPE_CV_JP_BB] = res.getString(R.string.modtype_captainvoice_ja_bb)
        modType[ModPackageInfo.SUB_MODTYPE_CV_JP_CA] = res.getString(R.string.modtype_captainvoice_ja_ca)
        modType[ModPackageInfo.SUB_MODTYPE_CV_JP_DD] = res.getString(R.string.modtype_captainvoice_ja_dd)
        modType[ModPackageInfo.SUB_MODTYPE_CV_DE] = res.getString(R.string.modtype_captainvoice_de)
        modType[ModPackageInfo.SUB_MODTYPE_CV_RU] = res.getString(R.string.modtype_captainvoice_ru)
        modType[ModPackageInfo.SUB_MODTYPE_CV_RU_VLAD] = res.getString(R.string.modtype_captainvoice_ru_vlad)
        modType[ModPackageInfo.SUB_MODTYPE_CV_RU_BEARD] = res.getString(R.string.modtype_captainvoice_ru_beard)
        modType[ModPackageInfo.MOSTYPE_CUSTOMSHIPNAME] = res.getString(R.string.modtype_customshipname)

        inited = true
        Logger.i("ModPackageManagerV2 initialized")
    }

    fun getModTypeName(type: String): String? {
        if (modType.containsKey(type)) {
            return modType[type]
        }
        return modType[ModPackageInfo.MODTYPE_OTHER]
    }

    private fun writeConfig() {
        thread(start = true) {
            Logger.i("Writing mod config...")
            synchronized(duplicatedFileInfo) {
                synchronized(modList) {
                    val config = Config(version = managerVer, isOverride = override, modInfos = modList.toMutableList(), duplicationInfos = duplicatedFileInfo.toMutableSet())
                    Utils.ensureFileParent(dataFile)
                    if (!dataFile.parentFile.canWrite()) {
                        return@thread
                    }
                    val sink = dataFile.sink().buffer()
                    sink.writeUtf8(json.stringify(Config.serializer(), config))
                    sink.close()
                }
            }
        }
    }

    interface OnDataChangedListener {
        fun onChange()
    }

    @Serializable
    data class ModInstallationInfo(val name: String, var type: String, var subType: String, var version: Int, var status: Status, var files: MutableSet<String>) {}

    @Serializable
    data class Config(var version: Int = managerVer, var isOverride: Boolean, var modInfos: MutableList<ModInstallationInfo>, var duplicationInfos: MutableSet<DuplicationInfo> = mutableSetOf())

    /**
     * conflictList :list of files duplicated
     */
    data class QueryResult(var result: Int = 0, var conflictList: Set<String>) {
        companion object {
            const val RESULT_OK = 0
            const val RESULT_UPDATE = 1
            const val RESULT_CONFLICT = 2
        }
    }

    data class PendingInstallation(val name: String, val type: String, val subType: String, val isUpdate: Boolean)
    enum class Status(val status: Int) {
        INSTALLING(10), INSTALLED(20), PARTLY_WORKING(21), UNKNOWN(-2);
    }

    @Serializable
    data class DuplicatedFile(val modName: String, var currFileName: String)

    /**
     * the oldest will be the first item in the list
     * and the latest will be the last
     */
    @Serializable
    data class DuplicationInfo(val fileName: String, var files: MutableList<DuplicatedFile> = mutableListOf())


    @Suppress("DEPRECATION")
    class MigrationHelper(private val activity: Main) : AsyncTask<File, String, Boolean>() {
        private lateinit var progressDialog: ProgressDialog
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(activity)
            progressDialog.setTitle("Migrating")
            progressDialog.setMessage(application.getString(R.string.please_wait))
            progressDialog.max = ModPackageManager.PUBLIC_KEYS.size
            progressDialog.setCancelable(false)
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
        }

        override fun doInBackground(@NonNull vararg params: File?): Boolean {
            val mods = ModPackageManager.getInstance().modList
            mods.forEach {
                if (!it.value.isBlank()) {
                    var type = it.key
                    var subType = ModPackageInfo.SUBTYPE_EMPTY
                    if (it.key.startsWith("CV")) {
                        type = ModPackageInfo.MODTYPE_CV
                        subType = it.key
                    }
                    val parentDirPath = ModPackageInstallHelper.getPath(type, ModPackageInstallHelper.getSubTypeId(subType), application)
                    val prefix = parentDirPath + File.separatorChar
                    val fileNames = listFiles(File(parentDirPath))
                    val files = mutableSetOf<String>()
                    fileNames.forEach {
                        files.add(it.removePrefix(prefix))
                    }
                    modList.add(ModInstallationInfo(it.value, type, subType, -1, ModPackageManagerV2.Status.INSTALLED, files))
                }
                publishProgress()
            }
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            params[0]?.delete()
            return true
        }

        private fun listFiles(dir: File): MutableSet<String> {
            val filenames = mutableSetOf<String>()
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            if (!dir.exists()) {
                return mutableSetOf()
            }
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            for (file in dir.listFiles()) {
                if (file.isFile) {
                    filenames.add(file.path)
                } else {
                    filenames += listFiles(file)
                }
            }
            return filenames
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            progressDialog.incrementProgressBy(1)
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            refresh()
            progressDialog.dismiss()
        }

    }
}