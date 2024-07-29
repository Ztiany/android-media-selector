package com.android.sdk.mediaselector

import android.net.Uri

interface ResultListener {

    fun onResult(result: List<Uri>)

    /** Check out logs for detailed information. */
    fun onFailed() {}

    fun onCanceled() {}

}