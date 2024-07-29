package com.android.sdk.mediaselector.processor

import android.net.Uri

interface ProcessorChain {

    fun onFailed()

    fun onCanceled()

    fun onResult(uri: List<Uri>)

}