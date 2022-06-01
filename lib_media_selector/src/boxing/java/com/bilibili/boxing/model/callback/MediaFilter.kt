package com.bilibili.boxing.model.callback

import android.net.Uri
import android.os.Parcelable

/**
 *@author Ztiany
 */
interface MediaFilter : Parcelable {

    /** Returning true means the file represented by this [uri] will be discarded. */
    fun filterUri(uri: Uri): Boolean

    /** Returning true means the file whose size is bigger than the specified will be discarded. */
    fun filerSize(size: Long): Boolean

}