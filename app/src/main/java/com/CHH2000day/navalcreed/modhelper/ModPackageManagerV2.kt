package com.CHH2000day.navalcreed.modhelper

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okio.source
import java.io.File

object ModPackageManagerV2 {
    private const val managerVer = 2
    private lateinit var dataFile: File
    private var inited = false
    private var isOverride = false
    private var modSet = mutableSetOf<ModInstallationInfo>()
    private var pendingTask: PendingInstallation? = null
    private var installationFiles: MutableSet<String> = mutableSetOf();
    private const val CONFLICT_SUFFIX = ".old"
    private lateinit var duplicatedFileInfo: MutableSet<DuplicationInfo>
    private lateinit var application: ModHelperApplication
    private val installConflictFiles = mutableSetOf<String>()
    private var onDataChangedListener: OnDataChangedListener? = null


    public fun config(file: File, application: ModHelperApplication): Boolean {
        dataFile = file
        this.application = application
        init()
        return true
    }

    public fun registerOnDataChangeListener(listener: OnDataChangedListener) {
        onDataChangedListener = listener
    }

    public fun unregisterOnDataChangeListener() {
        onDataChangedListener = null
    }

    /**
     * Check if there's a conflict with existing mod
     */
    public fun checkInstall(name: String, type: String, subType: String, version: Int, files: MutableSet<String>): QueryResult {
        val result = QueryResult(result = QueryResult.RESULT_OK, conflictList = mutableSetOf())
        for (mod in modSet) {
            lateinit var resultSet: Set<String>
            if (mod.type == type) {
                if (mod.name == name && mod.version <= version) {
                    result.result = QueryResult.RESULT_UPDATE
                    return result
                }
                if ((type != ModPackageInfo.MODTYPE_CV) || (type == ModPackageInfo.MODTYPE_CV && subType == mod.subType)) {
                    resultSet = mod.files intersect files
                    if (resultSet.isNotEmpty()) {
                        result.result = QueryResult.RESULT_CONFLICT
                        result.conflictList = resultSet
                        return result
                    }
                }
            }
        }
        return result
    }

    @Synchronized
    public fun uninstall(name: String): Int {
        val installation = getInstallation(name) ?: return -10
        for (file in installation.files) {
            recoverFileFromConflict(file, installation)
        }
        modSet.remove(installation)
        refresh()
        return 0;
    }

    public fun refresh() {
        onDataChangedListener?.onChange()
        writeConfig()
    }

    private fun recoverFileFromConflict(fileName: String, installation: ModInstallationInfo) {
        val basePath = getBasePath(installation)
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
                    } else {
                        //remove file as it's the last item in list.No necessary to modify installation info
                        File(basePath, fileInfo.currFileName).delete()
                        info.files.remove(fileInfo)
                        if (info.fileName.isEmpty()) {
                            duplicatedFileInfo.remove(info)
                        }
                    }
                }
                //End this method
                return
            }
        }
        //If no duplication
        File(basePath, fileName).delete()
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
                    } else {
                        //remove file as it's the last item in list.
                        //Modify installation info to partly working
                        File(basePath, fileInfo.currFileName).delete()
                        info.files.remove(fileInfo)
                        if (info.files.size == 1) {
                            getInstallation(info.files[0].modName)?.status = Status.INSTALLED.status
                            duplicatedFileInfo.remove(info)
                        }
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
    public fun renameConflict(name: String) {
        installConflictFiles.add(name)
        for (info in duplicatedFileInfo) {
            if (info.fileName == name) {
                doRenameConflict(name, info)
                return
            }
        }
        doRenameConflict(name)
    }

    private fun doRenameConflict(name: String) {
        val duplicationInfo = DuplicationInfo(name)
        duplicatedFileInfo.add(duplicationInfo)
        marker@ for (mod in modSet) {
            for (file in mod.files) {
                if (name == file) {
                    duplicationInfo.files.add(DuplicatedFile(mod.name, name))
                    break@marker
                }
            }
        }
        doRenameConflict(name, duplicationInfo)
    }

    private fun doRenameConflict(name: String, info: DuplicationInfo) {
        for (duplicatedFile in info.files) {
            //Get installation info of existed mod,return if has null
            val installation = getInstallation(duplicatedFile.modName) ?: return
            //Get base path
            val basePath = getBasePath(installation)
            //Rename it to $OLDNAME.old
            File(basePath, duplicatedFile.currFileName).renameTo(File(basePath, duplicatedFile.currFileName + CONFLICT_SUFFIX))
            //Update name in existed installation info
            installation.files.remove(duplicatedFile.currFileName)
            duplicatedFile.currFileName += CONFLICT_SUFFIX
            installation.files.add(duplicatedFile.currFileName)
            //Set status to partly working
            installation.status = Status.PARTLY_WORKING.status
        }
        info.files.add(DuplicatedFile(pendingTask!!.name, name))
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

    public fun getMods(): Set<ModInstallationInfo> = modSet.toSet()

    public fun getInstallation(name: String): ModInstallationInfo? {
        for (mod in modSet) {
            if (mod.name == name) {
                return mod
            }
        }
        return null
    }

    public fun getInstallation(type: String, subType: String): ModInstallationInfo? {
        for (mod in modSet) {
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
    public fun requestInstall(name: String, type: String, subType: String): Boolean {
        if (pendingTask == null) {
            var isUpdate = false
            for (mod in modSet) {
                if (mod.name == name) {
                    isUpdate = true
                    mod.status = Status.INSTALLING.status
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
    public fun onFileInstalled(name: String) {
        installationFiles.add(name)
    }

    /**
     * Call this once installation is done
     */
    public fun postInstall(version: Int) {
        if (pendingTask != null) {
            if (pendingTask!!.isUpdate) {
                val installation: ModInstallationInfo? = getInstallation(pendingTask!!.name)
                installation?.status = Status.INSTALLED.status
                installation?.files?.plusAssign(installationFiles!!)
                modSet.add(installation!!)
            } else {
                val installationInfo = ModInstallationInfo(name = pendingTask!!.name, type = pendingTask!!.type, subType = pendingTask!!.subType, version = version, status = Status.INSTALLED.status, files = installationFiles!!)
                modSet.add(installationInfo)
            }
        }
        pendingTask = null
        installationFiles.clear()
        refresh()
    }

    public fun onInstallFail() {
        pendingTask = null
        refresh()
    }

    @Synchronized
    public fun rollback() {
        for (filename in installConflictFiles) {
            recoverFileFromConflict(filename)
        }
        val basePath = getBasePath()
        for (filename in installationFiles) {
            File(basePath, filename).delete()
        }
    }

    private fun init() {
        val source = dataFile.source().buffer()
//        val type = object : TypeToken<List<ModInstallationInfo>>() {}.type
        val config = GsonHelper.getGson().fromJson(source.readUtf8(), Config::class.javaObjectType)
        isOverride = config.isOverride
        modSet = config.modInfos
        duplicatedFileInfo = config.duplicationInfos
        source.close()
        inited = true
    }

    private fun writeConfig() {
        GlobalScope.launch {
            val mods = modSet.toSet()
            synchronized(duplicatedFileInfo) {}
            val config = Config(version = managerVer, isOverride = isOverride, modInfos = modSet, duplicationInfos = duplicatedFileInfo)
            val sink = dataFile.sink().buffer()
            sink.writeUtf8(GsonHelper.getGson().toJson(config))
            sink.close()

        }.start()

    }

    interface OnDataChangedListener {
        fun onChange()
    }

    data class ModInstallationInfo(val name: String, var type: String, var subType: String, var version: Int, var status: Int, var files: MutableSet<String>) {}
    data class Config(var version: Int = managerVer, var isOverride: Boolean, var modInfos: MutableSet<ModInstallationInfo>, var duplicationInfos: MutableSet<DuplicationInfo> = mutableSetOf())
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

    data class DuplicatedFile(val modName: String, var currFileName: String)
    /**
     * the oldest will be the first item in the list
     * and the latest will be the last
     */
    data class DuplicationInfo(val fileName: String, var files: MutableList<DuplicatedFile> = mutableListOf())
}