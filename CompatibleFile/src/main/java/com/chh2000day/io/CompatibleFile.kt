package com.chh2000day.io

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import okio.*
import java.io.File

@Suppress("unused")
class CompatibleFile(
    rawFile: File,
    private val compatibleFileContext: CompatibleFileContext
) :
    File(rawFile.absolutePath) {
    companion object {
        private val filenameRegex = Regex(".+/Android/data*")
    }

    private val shouldUseSAF =
        compatibleFileContext.android11Flag && this.absolutePath.contains(filenameRegex)

    override fun createNewFile(): Boolean {
        return if (shouldUseSAF) {
            kotlin.runCatching {
                toDocumentFile()
            }.isSuccess
        } else {
            super.createNewFile()
        }
    }

    override fun canExecute(): Boolean {
        return if (shouldUseSAF) {
            false
        } else {
            super.canExecute()
        }
    }

    override fun canRead(): Boolean {
        return if (shouldUseSAF) {
            toDocumentFileOrNull()?.canRead() == true
        } else {
            super.canRead()
        }
    }

    override fun canWrite(): Boolean {
        return if (shouldUseSAF) {
            toDocumentFileOrNull()?.canWrite() == true
        } else {
            super.canWrite()
        }
    }

    override fun delete(): Boolean {
        return if (shouldUseSAF) {
            toDocumentFileOrNull()?.delete() == true
        } else {
            super.delete()
        }
    }

    override fun deleteOnExit() {
        if (!shouldUseSAF) {
            super.deleteOnExit()
        }
    }

    override fun exists(): Boolean {
        return if (shouldUseSAF) {
            toDocumentFileOrNull()?.exists() == true
        } else {
            super.exists()
        }
    }

    override fun isDirectory(): Boolean {
        return if (shouldUseSAF) {
            toDocumentFileOrNull()?.isDirectory == true
        } else {
            super.isDirectory()
        }
    }

    override fun isFile(): Boolean {
        return if (shouldUseSAF) {
            toDocumentFileOrNull()?.isFile == true
        } else {
            super.isFile()
        }
    }

    override fun listFiles(): Array<File>? {
        return if (shouldUseSAF) {
            getDocumentParentOrNull()?.listFiles()?.map {
                it.uri.resolveFilePath() ?: ""
            }?.filter {
                it.isNotBlank()
            }?.map {
                File(it)
            }?.toTypedArray()
        } else {
            super.listFiles()
        }
    }

    override fun list(): Array<String>? {
        return if (shouldUseSAF) {
            listFiles()?.map {
                it.path
            }?.toTypedArray()
        } else {
            super.list()
        }
    }

    override fun mkdir(): Boolean {
        return if (shouldUseSAF) {
            kotlin.runCatching {
                this.getDocumentParent()
            }.isSuccess
        } else {
            super.mkdir()
        }
    }

    override fun mkdirs(): Boolean {
        return if (shouldUseSAF) {
            kotlin.runCatching {
                this.toDocumentDir()
            }.isSuccess
        } else {
            super.mkdirs()
        }
    }

    override fun renameTo(dest: File): Boolean {
        return if (shouldUseSAF) {
            kotlin.runCatching {
                toDocumentFileOrNull()?.renameTo(dest.path)
            }.getOrElse { false }!!
        } else {
            super.renameTo(dest)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun toSource(): Source {
        return if (shouldUseSAF) {
            compatibleFileContext.context.contentResolver.openInputStream(
                toDocumentFile().uri
            )!!.source()
        } else {
            this.source()
        }
    }

    fun toBufferedSource(): BufferedSource {
        return toSource().buffer()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun toSink(): Sink {
        return if (shouldUseSAF) {
            compatibleFileContext.context.contentResolver.openOutputStream(
                toDocumentFile().uri
            )!!.sink()
        } else {
            this.sink()
        }
    }

    fun toBufferedSink(): BufferedSink {
        return toSink().buffer()
    }

    private fun Uri.resolveFilePath(): String? {
        if (path == null || authority == null) {
            return null
        }
        //如果path已为绝对路径，直接返回
        if (path!!.startsWith("/storage")) {
            return path
        }
        //如果为SAF返回的数据，解码
        @Suppress("SpellCheckingInspection")
        if (authority == "com.android.externalstorage.documents") {
            val docId = DocumentsContract.getDocumentId(this)
            val split = docId?.split(":", limit = 2)?.toTypedArray()
            if (split?.size ?: -1 >= 2) {
                val type = split?.get(0)
                if ("primary".equals(type, ignoreCase = true)) {
                    @Suppress("DEPRECATION")
                    return Environment.getExternalStorageDirectory().absolutePath + separator + (split?.get(
                        1
                    ) ?: "")
                } else if ("secondary".equals(type, ignoreCase = true)) {

                    return System.getenv("SECONDARY_STORAGE") ?: "" + separator + (split?.get(1)
                        ?: "")
                } else {
                    val volId = split?.get(0)?.split(separatorChar.toString())?.toTypedArray()
                    val vol = volId?.get(volId.size - 1)
                    if (vol?.contains("-") == true) {
                        return separatorChar.toString() + "storage" + separatorChar + vol + separatorChar + split[1]
                    }
                }
            }
        }
        if (authority.equals("com.android.providers.downloads.documents", ignoreCase = true)) {
            val paths = path!!.split("raw:", limit = 2).toTypedArray()
            if (paths.size == 2) {
                return paths[1]
            }
        }
        return null
    }

    private fun File.toDocumentFileUri(): Uri {
        @Suppress("SpellCheckingInspection") val stringBuilder =
            StringBuilder("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata")
        this.absolutePath.replace("/storage/emulated/0/Android/data", "").split("/").forEach {
            stringBuilder.append(it)
        }
        return Uri.parse(stringBuilder.toString())
    }

    /**
     * Also includes mkdir()
     */
    private fun File.getDocumentParent(): DocumentFile {
        if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
            throw UnsupportedOperationException("This method is only a compatible layer for Android only:path:$absolutePath")
        }
        var docFile: DocumentFile? =
            DocumentFile.fromTreeUri(
                compatibleFileContext.context,
                CompatibleFileContext.initialUri
            )
                ?: throw IllegalStateException("Failed to access Android/data,PERMISSION DENIED")
        var pathBuffer = "/storage/emulated/0/Android/data"
        //Do mkdir
        this.parentFile!!.absolutePath.replace("/storage/emulated/0/Android/data", "")
            .split("/")
            .forEach { name ->
                if (name.isNotEmpty()) {
                    var nextElement = docFile?.listFiles()?.findLast {
                        it.name == name
                    }
                    pathBuffer += "name"
                    if (nextElement == null) {
                        nextElement = docFile?.createDirectory(name)
                    }
                    docFile = nextElement
                }
            }
        return docFile!!
    }

    private fun File.getDocumentParentOrNull(): DocumentFile? {
        if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
            throw UnsupportedOperationException("This method is only a compatible layer for Android only")
        }
        var docFile = DocumentFile.fromTreeUri(
            compatibleFileContext.context,
            CompatibleFileContext.initialUri
        )
        this.parentFile!!.absolutePath.replace("/storage/emulated/0/Android/data", "")
            .split("/")
            .forEach { name ->
                if (name.isNotEmpty()) {
                    val nextElement: DocumentFile = docFile?.listFiles()?.findLast {
                        it.name == name
                    } ?: return null
                    docFile = nextElement
                }
            }
        return docFile
    }

    @JvmOverloads
    fun File.toDocumentFile(
        mime: String = "*/*"
    ): DocumentFile {
        if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
            throw UnsupportedOperationException("This method is only a compatible layer for Android only")
        }
        val parent = getDocumentParent()
        return parent.listFiles().firstOrNull {
            it.name == name
        } ?: return run {
            parent.createFile(mime, name)!!
        }
    }

    private fun File.toDocumentFileOrNull(): DocumentFile? {
        if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
            throw UnsupportedOperationException("This method is only a compatible layer for Android only")
        }
        val parent = getDocumentParentOrNull()
        return parent?.listFiles()?.firstOrNull {
            it.name == name
        }
    }


    /**
     * Caution:This also contains mkdirs()
     */
    private fun File.toDocumentDir(): DocumentFile {
        if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
            throw UnsupportedOperationException("This method is only a compatible layer for Android only")
        }
        val parent = getDocumentParent()
        return parent.listFiles().firstOrNull {
            it.name == name
        } ?: kotlin.run {
            parent.createDirectory(name)!!
        }
    }

    private fun File.toDocumentDirOrNull(): DocumentFile? {
        if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
            throw UnsupportedOperationException("This method is only a compatible layer for Android only")
        }
        val parent = getDocumentParentOrNull()
        return parent?.listFiles()?.firstOrNull {
            it.name == name
        }
    }
}