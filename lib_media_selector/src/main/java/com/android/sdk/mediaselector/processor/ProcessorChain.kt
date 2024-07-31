package com.android.sdk.mediaselector.processor

import com.android.sdk.mediaselector.Item

interface ProcessorChain {

    fun onFailed()

    fun onCanceled()

    fun onResult(items: List<Item>)

}