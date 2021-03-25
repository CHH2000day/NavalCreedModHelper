package com.CHH2000day.navalcreed.modhelper

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import okio.*
import java.io.File

private fun File.toDocumentFileUri(): Uri {
    @Suppress("SpellCheckingInspection") val stringBuilder =
        StringBuilder("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata")
    this.absolutePath.replace("/storage/emulated/0/Android/data", "").split("/").forEach {
        stringBuilder.append(it)
    }
    return Uri.parse(stringBuilder.toString())
}

fun File.toDocumentFile(context: Context = ModHelperApplication.getModHelperApplication()): DocumentFile =
    DocumentFile.fromSingleUri(context, toDocumentFileUri())!!

fun File.toBufferedSource(context: Context = ModHelperApplication.getModHelperApplication()): BufferedSource {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        context.contentResolver.openInputStream(
            toDocumentFile(context).uri
        )!!.source().buffer()
    } else {
        source().buffer()
    }
}

fun File.toBufferedSink(context: Context = ModHelperApplication.getModHelperApplication()): BufferedSink {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        context.contentResolver.openOutputStream(
            toDocumentFile(context).uri
        )!!.sink().buffer()
    } else {
        sink().buffer()
    }
}