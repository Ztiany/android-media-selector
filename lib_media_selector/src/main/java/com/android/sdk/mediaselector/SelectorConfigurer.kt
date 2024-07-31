package com.android.sdk.mediaselector

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.R
import com.android.sdk.mediaselector.permission.MediaPermissionRequester
import com.android.sdk.mediaselector.permission.PermissionXImpl
import timber.log.Timber

class SelectorConfigurer {

    fun setAuthority(authority: String) {
        mediaAuthority = authority
    }

    fun setPermissionRequester(requester: MediaPermissionRequester) {
        mediaPermissionRequester = requester
    }

    fun setUp() {
        Timber.d("SelectorConfigurer setUp!")
    }

}

private var boxingInitialized = false
private var mediaAuthority: String = ""
private var mediaPermissionRequester: MediaPermissionRequester = PermissionXImpl()

internal fun getConfiguredAuthority(context: Context): String {
    if (mediaAuthority.isNotEmpty()) {
        return mediaAuthority
    }
    return context.packageName + ".file.provider"
}

@ColorInt
internal fun Context.getConfiguredPrimaryColor(): Int {
    val outValue = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimary, outValue, true)
    return Color.RED
}

internal fun getPermissionRequester(): MediaPermissionRequester {
    return mediaPermissionRequester
}