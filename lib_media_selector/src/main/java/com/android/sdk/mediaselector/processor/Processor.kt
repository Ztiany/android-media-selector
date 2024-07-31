package com.android.sdk.mediaselector.processor

import android.net.Uri
import com.android.sdk.mediaselector.ComponentStateHandler
import com.android.sdk.mediaselector.Item

interface Processor : ComponentStateHandler {

    fun onAttachToChain(processorChain: ProcessorChain)

    fun start(params: List<Item>)

}