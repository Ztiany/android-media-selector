package com.android.sdk.mediaselector.common

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 *@author Ztiany
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun copySingleToInternal(context: Context, uri: Uri): String? {
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null, null)
    var postfix = cursor?.use {
        if (it.moveToFirst()) {
            val displayIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            StorageUtils.getFileExtension(it.getString(displayIndex))
        } else ""
    }

    // TODO  get file extension via reading binary magic number.
    if (postfix.isNullOrBlank()) {
        postfix = StorageUtils.JPEG
    }

    return try {
        val target = StorageUtils.createInternalPicturePath(context, postfix)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            Files.copy(inputStream, Paths.get(target))
        }
        target
    } catch (e: Exception) {
        Timber.d("copySingleToInternal() $e")
        null
    }
}

fun newUriList(filePath: String): List<Uri> {
    val result: MutableList<Uri> = ArrayList()
    result.add(Uri.fromFile(File(filePath)))
    return result
}