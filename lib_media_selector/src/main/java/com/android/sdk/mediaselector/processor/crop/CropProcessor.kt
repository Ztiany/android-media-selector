package com.android.sdk.mediaselector.processor.crop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.ProcessorChain
import com.android.sdk.mediaselector.utils.ActFragWrapper
import com.android.sdk.mediaselector.utils.MediaUtils
import timber.log.Timber
import java.io.File

internal class CropProcessor(
    private val host: ActFragWrapper,
    private val cropOptions: CropOptions,
) : Processor {

    override var processorChain: ProcessorChain? = null

    private var originContent: List<Uri> = emptyList()

    private var processedContent = mutableListOf<Uri>()

    private var progress = 0

    override fun start(params: List<Uri>) {
        originContent = params
        progress = 0
        continueCropWork()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CROP) {
            return
        }
        if (resultCode != Activity.RESULT_OK || data == null) {
            processorChain?.onCanceled()
            return
        }
        processCropResult(data)
    }

    private fun processCropResult(data: Intent) {
        val uCropResult = MediaUtils.getUCropResult(data)
        Timber.d("processCropResult() called with: data = [$uCropResult]")
        if (uCropResult == null) {
            processorChain?.onFailed()
            return
        }
        val absolutePath = MediaUtils.getAbsolutePath(host.context, uCropResult)
        Timber.d("processCropResult() called with: absolutePath = [$absolutePath]")
        if (TextUtils.isEmpty(absolutePath)) {
            processorChain?.onFailed()
            return
        }

        processedContent.add(Uri.fromFile(File(absolutePath)))
        continueCropWork()
    }

    private fun continueCropWork() {
        if (progress >= originContent.size) {
            processorChain?.onResult(processedContent)
            return
        }

        MediaUtils.toUCrop(
            host,
            originContent[progress],
            cropOptions,
            REQUEST_CROP
        )

        progress++
    }

    companion object {
        private const val REQUEST_CROP = 10902
    }

}