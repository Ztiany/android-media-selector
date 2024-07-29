package com.android.sdk.mediaselector.processor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.android.sdk.mediaselector.ActivityStateHandler

interface Processor : ActivityStateHandler {

    var processorChain: ProcessorChain?

    fun start(params: List<Uri>)

    override fun onSaveInstanceState(outState: Bundle) = Unit

    override fun onRestoreInstanceState(outState: Bundle?) = Unit

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit

}