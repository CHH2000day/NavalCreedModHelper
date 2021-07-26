package com.CHH2000day.navalcreed.modhelper

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.orhanobut.logger.Logger
import okio.*
import java.io.File

internal val android11Flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

@Suppress("SpellCheckingInspection")
private val initialUri =
    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")

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
fun File.getDocumentParent(context: Context = ModHelperApplication.getModHelperApplication()): DocumentFile {
    if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
        throw UnsupportedOperationException("This method is only a compatible layer for Android only")
    }
    var docFile = DocumentFile.fromTreeUri(context, initialUri)
    var pathBuffer = "/storage/emulated/0/Android/data"
    //Do mkdir
    this.parentFile!!.absolutePath.replace("/storage/emulated/0/Android/data", "").split("/")
        .forEach { name ->
            if (name.isNotEmpty()) {
                var nextElement = docFile?.listFiles()?.findLast {
                    it.name == name
                }
                pathBuffer += "name"
                if (nextElement == null) {
                    Logger.d("Creating dir:$pathBuffer")
                    nextElement = docFile?.createDirectory(name)
                }
                docFile = nextElement
            }
        }
    return docFile!!
}

fun File.getDocumentParentOrNull(context: Context = ModHelperApplication.getModHelperApplication()): DocumentFile? {
    if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
        throw UnsupportedOperationException("This method is only a compatible layer for Android only")
    }
    var docFile = DocumentFile.fromTreeUri(context, initialUri)
    this.parentFile!!.absolutePath.replace("/storage/emulated/0/Android/data", "").split("/")
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
    context: Context = ModHelperApplication.getModHelperApplication(),
    mime: String = "*/*"
): DocumentFile {
    if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
        throw UnsupportedOperationException("This method is only a compatible layer for Android only")
    }
    val parent = getDocumentParent(context)
    return parent.listFiles().firstOrNull {
        it.name == name
    } ?: return run {
        Logger.d("creating file:${this.absoluteFile};MIME:$mime")
        parent.createFile(mime, name)!!
    }
}

fun File.toDocumentFileOrNull(context: Context = ModHelperApplication.getModHelperApplication()): DocumentFile? {
    if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
        throw UnsupportedOperationException("This method is only a compatible layer for Android only")
    }
    val parent = getDocumentParentOrNull(context)
    return parent?.listFiles()?.firstOrNull {
        it.name == name
    }
}


/**
 * Caution:This also contains mkdirs()
 */
fun File.toDocumentDir(context: Context = ModHelperApplication.getModHelperApplication()): DocumentFile {
    if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
        throw UnsupportedOperationException("This method is only a compatible layer for Android only")
    }
    val parent = getDocumentParent(context)
    return parent.listFiles().firstOrNull {
        it.name == name
    } ?: kotlin.run {
        Logger.d("Creating dir :${this.absolutePath}")
        parent.createDirectory(name)!!
    }
}

fun File.toDocumentDirOrNull(context: Context = ModHelperApplication.getModHelperApplication()): DocumentFile? {
    if (!this.absolutePath.contains(Regex(".+/Android/data*"))) {
        throw UnsupportedOperationException("This method is only a compatible layer for Android only")
    }
    val parent = getDocumentParentOrNull(context)
    return parent?.listFiles()?.firstOrNull {
        it.name == name
    }
}

@JvmOverloads
fun File.toBufferedSource(context: Context = ModHelperApplication.getModHelperApplication()): BufferedSource {
    return if (android11Flag) {
        context.contentResolver.openInputStream(
            toDocumentFile(context).uri
        )!!.source().buffer()
    } else {
        source().buffer()
    }
}

@JvmOverloads
fun File.toBufferedSink(context: Context = ModHelperApplication.getModHelperApplication()): BufferedSink {
    return if (android11Flag) {
        context.contentResolver.openOutputStream(
            toDocumentFile(context).uri
        )!!.sink().buffer()
    } else {
        sink().buffer()
    }
}

/**
 * Android 11 Compatible method
 */
fun File.existsCompatible(): Boolean = if (android11Flag) {
    toDocumentFile().exists()
} else {
    exists()
}

/**
 * This is only a compatible layer to access /Android
 */
fun File.mkdirCompatible() {
    if (android11Flag) {
        this.getDocumentParent()
    } else {
        this.mkdir()
    }
}

fun File.createFileCompatible(mime: String = "*/*") {
    if (android11Flag) {
        this.toDocumentFile(mime = mime)
    } else {
        this.createNewFile()
    }
}