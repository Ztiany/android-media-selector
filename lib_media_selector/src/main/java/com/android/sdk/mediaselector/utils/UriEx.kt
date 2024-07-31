package com.android.sdk.mediaselector.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import com.android.sdk.mediaselector.Item
import timber.log.Timber

internal data class FileAttribute(
    val name: String,
    val size: Long,
)

internal fun Uri.setRequireOriginal(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            MediaStore.setRequireOriginal(this)
        } else {
            Timber.w("You don't have the permission ACCESS_MEDIA_LOCATION to access media location.")
        }
    }
}

internal fun Uri.isFile(): Boolean {
    return scheme == "file"
}

internal fun Uri.getAttribute(context: Context): FileAttribute? {
    return if (this.isFile()) {
        getFileAttribute(context)
    } else {
        getUriAttribute(context)
    }
}

private fun Uri.getFileAttribute(context: Context): FileAttribute {
    val file = toFile()
    return FileAttribute(file.name, file.length())
}

private fun Uri.getUriAttribute(context: Context): FileAttribute? {
    context.contentResolver.query(this@getUriAttribute, null, null, null, null, null)?.use {
        // moveToFirst() returns false if the cursor has 0 rows. Very handy for
        // "if there's anything to look at, look at it" conditionals.
        if (it.moveToFirst()) {
            val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val displayName: String = it.getString(columnIndex)

            val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
            val size = if (!it.isNull(sizeIndex)) {
                it.getString(sizeIndex).toLongOrNull() ?: 0L
            } else 0L
            Timber.d("getAttribute: $displayName, $size")
            return FileAttribute(displayName, size)
        }
    }

    return null
}

/**
 * refers to:
 *
 * - [get-real-path-from-uri-android-kitkat-new-storage-access-framework](https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework/20559175)
 * - [get-filename-and-path-from-uri-from-media-store](https://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore)
 * - [how-to-get-the-full-file-path-from-uri](https://stackoverflow.com/questions/13209494/how-to-get-the-full-file-path-from-uri)
 */
internal fun Uri.getAbsolutePath(context: Context): String? {
    return MediaUtils.getAbsolutePath(context, this@getAbsolutePath)
}

internal fun Uri.getFilePostfix(context: Context): String? {
    return getAttribute(context)?.name?.substringAfterLast('.')?.lowercase()
}

// TODO: read the file content to determine the file type, instead of relying on the file extension.
internal fun Uri.isCropSupported(context: Context): Boolean {
    val postfix = getFilePostfix(context) ?: return false
    return MineType.IMAGE.formats.contains(postfix)
}

// TODO: fill the media info for the item.
internal fun Item.tryFillMediaInfo(context: Context): Item {
    if (uri.isFile()) {
        return copy(rawPath = uri.toFile().absolutePath)
    }
    return this
}