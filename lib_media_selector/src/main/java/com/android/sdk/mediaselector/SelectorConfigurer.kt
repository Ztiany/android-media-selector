package com.android.sdk.mediaselector

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.R
import com.android.sdk.mediaselector.imageloader.BoxingGlideLoader
import com.android.sdk.mediaselector.permission.MediaPermissionRequester
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.loader.IBoxingMediaLoader
import com.bilibili.boxing.utils.BoxingFileHelper
import timber.log.Timber

class SelectorConfigurer {

    fun setAuthority(authority: String) {
        sAuthority = authority
    }

    /**
     * Using [glide](https://github.com/bumptech/glide) as the media loader in default. you can change it by providing your own [IBoxingMediaLoader].
     */
    fun setImageLoader(boxingMediaLoader: IBoxingMediaLoader) {
        boxingInitialized = true
        BoxingMediaLoader.getInstance().init(boxingMediaLoader)
    }

    /**
     * The folder name where the camera photo is saved.
     */
    fun setCameraPhotoFolderName(folderName: String) {
        BoxingFileHelper.DEFAULT_SUB_DIR = folderName
    }

    fun setPermissionRequester(requester: MediaPermissionRequester) {
        storageAccessPermission = requester
    }

    fun setUp() {
        Timber.d("SelectorConfigurer setUp!")
    }

}

private var sAuthority: String = ""
private var boxingInitialized = false
private var storageAccessPermission: MediaPermissionRequester? = null

internal fun getConfiguredAuthority(context: Context): String {
    if (sAuthority.isNotEmpty()) {
        return sAuthority
    }
    return context.packageName + ".file.provider"
}

internal fun initializeBoxIfNeed() {
    if (!boxingInitialized) {
        BoxingMediaLoader.getInstance().init(BoxingGlideLoader())
        boxingInitialized = true
    }
}

@ColorInt
internal fun Context.getConfiguredPrimaryColor(): Int {
    val outValue = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimary, outValue, true)
    return Color.RED
}

internal fun getPermissionRequester(): MediaPermissionRequester {
    return storageAccessPermission ?: throw IllegalStateException("MediaPermissionRequester is not set!")
}