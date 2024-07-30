package com.android.sdk.mediaselector.processor

import android.net.Uri
import com.android.sdk.mediaselector.ComponentStateHandler

interface Processor : ComponentStateHandler {

    fun onAttachToChain(processorChain: ProcessorChain)

    fun start(params: List<Uri>)

}