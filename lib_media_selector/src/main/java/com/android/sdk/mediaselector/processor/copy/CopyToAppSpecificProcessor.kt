package com.android.sdk.mediaselector.processor.copy

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.sdk.mediaselector.processor.BaseProcessor
import com.android.sdk.mediaselector.utils.createInternalPath
import com.android.sdk.mediaselector.utils.getFilePostfix
import timber.log.Timber
import java.nio.file.Files
import java.nio.file.Paths

class CopyToAppSpecificProcessor : BaseProcessor() {

    override fun start(params: List<Uri>) {

    }

    /**
     *@author Ztiany
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun copySingleToInternal(context: Context, uri: Uri): String? {
        // TODO  get file extension via reading binary magic number.
        var postfix = uri.getFilePostfix(context)
        if (postfix.isNullOrBlank()) {
            postfix = ".jpeg"
        }

        return try {
            val target = context.createInternalPath(postfix)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                Files.copy(inputStream, Paths.get(target))
            }
            target
        } catch (e: Exception) {
            Timber.d("copySingleToInternal() $e")
            null
        }
    }

}