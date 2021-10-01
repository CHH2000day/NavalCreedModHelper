package com.chh2000day.io

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

class CompatibleFileContext(internal val context: Context) {
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
    val android11Flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    companion object {
        @Suppress("SpellCheckingInspection")
        internal val initialUri =
            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")

        @SuppressLint("StaticFieldLeak")
        lateinit var defaultContext: CompatibleFileContext
        fun initContext(context: Context) {
            defaultContext = CompatibleFileContext(context)
        }

        fun initAndCheck(context: Context): Boolean {
            defaultContext = CompatibleFileContext(context)
            //Check writable
            val dataDir = DocumentFile.fromTreeUri(context, initialUri)
            return dataDir?.canWrite() == true
        }

        @ExperimentalCoroutinesApi
        suspend fun requestPermission(componentActivity: ComponentActivity): Boolean {
            return suspendCancellableCoroutine { coroutine ->
                val uri = DocumentFile.fromTreeUri(componentActivity, initialUri)?.uri
                val contract = object : ActivityResultContracts.OpenDocumentTree() {
                    override fun createIntent(context: Context, input: Uri?): Intent {
                        return super.createIntent(context, input).apply {
                            flags =
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                        }
                    }
                }
                componentActivity.registerForActivityResult(contract) {
                    coroutine.resume(it == uri, null)
                }.launch(uri)
            }
        }
    }
}