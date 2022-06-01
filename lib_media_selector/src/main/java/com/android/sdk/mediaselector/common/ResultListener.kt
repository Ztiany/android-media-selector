package com.android.sdk.mediaselector.common

import android.net.Uri

interface ResultListener {

    fun onTakeSuccess(result: List<Uri>)

    /** check out logs for detailed information. */
    fun onTakeFail() {}

    fun onCancel() {}

}