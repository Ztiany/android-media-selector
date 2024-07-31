package com.android.sdk.mediaselector.processor.copy

import android.content.Context
import android.net.Uri
import com.android.sdk.mediaselector.Item
import com.android.sdk.mediaselector.processor.BaseProcessor
import com.android.sdk.mediaselector.utils.createInternalPath
import com.android.sdk.mediaselector.utils.getFilePostfix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.io.FileOutputStream

class CopyToAppSpecificProcessor(private val context: Context) : BaseProcessor() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun start(params: List<Item>) {

    }

    private suspend fun copySingleToInternal(context: Context, uri: Uri): String? {
        // TODO  get file extension via reading binary magic number.
        var postfix = uri.getFilePostfix(context)
        if (postfix.isNullOrBlank()) {
            postfix = ".jpeg"
        }

        return try {
            val target = context.createInternalPath(postfix)
            val out = FileOutputStream(target)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(out)
            }
            runCatching { out.close() }
            target
        } catch (e: Exception) {
            Timber.d("copySingleToInternal() $e")
            null
        }
    }

}