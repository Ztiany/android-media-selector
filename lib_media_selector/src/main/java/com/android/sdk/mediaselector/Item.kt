package com.android.sdk.mediaselector

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val id: String,
    val uri: Uri,
    internal val middle: Map<String, Media>,
) : Parcelable

@Parcelize
data class Media(
    val uri: Uri,
    val size: Long = 0,
    val duration: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
) : Parcelable

fun Item.getMedia(processName: String): Media {
    return middle[processName] ?: throw IllegalArgumentException("processName not found")
}