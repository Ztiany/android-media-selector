package com.android.sdk.mediaselector.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.app.ActivityCompat
import com.android.sdk.mediaselector.Media
import timber.log.Timber

internal data class FileAttribute(
    val name: String,
    val size: Long,
)

internal fun Uri.getAttribute(context: Context): FileAttribute? {
    context.contentResolver.query(this, null, null, null, null, null)?.use {
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

internal fun Uri.getAbsolutePath(context: Context): String? {
    return MediaUtils.getAbsolutePath(context, this)
}

internal fun Uri.getFilePostfix(context: Context): String? {
    return getAttribute(context)?.name?.substringAfterLast('.')?.lowercase()
}

// TODO: read the file content to determine the file type, instead of relying on the file extension.
internal fun Uri.isCropSupported(context: Context): Boolean {
    val postfix = getFilePostfix(context) ?: return false
    return MineType.IMAGE.formats.contains(postfix)
}

internal fun Uri.setRequireOriginal(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            MediaStore.setRequireOriginal(this)
        } else {
            Timber.w("You don't have the permission ACCESS_MEDIA_LOCATION to access media location.")
        }
    }
}

internal fun Uri.tryExtractMediaInfo(context: Context): Media {
    return Media(uri = this)
}