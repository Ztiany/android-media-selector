package com.android.sdk.mediaselector.processor.capture

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.android.sdk.mediaselector.MediaSelectorConfiguration
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.ProcessorChain
import com.android.sdk.mediaselector.utils.ActFragWrapper
import com.android.sdk.mediaselector.utils.MediaUtils
import com.android.sdk.mediaselector.utils.newUriList
import timber.log.Timber
import java.io.File

internal class CapturePhotoProcessor(
    private val host: ActFragWrapper,
    private val savePath: String,
) : Processor {

    override var processorChain: ProcessorChain? = null

    override fun start(params: List<Uri>) {
        Timber.d("start is called with: $params")
        if (!MediaUtils.hasCamera(host.context)) {
            Timber.w("The device has no camera apps.")
            processorChain?.onFailed()
        }
        val targetFile = File(savePath)
        val intent = MediaUtils.makeCaptureIntent(host.context, targetFile, MediaSelectorConfiguration.getAuthority(host.context))
        try {
            host.startActivityForResult(intent, REQUEST_CAPTURE_PHOTO, null)
        } catch (e: Exception) {
            Timber.e(e, "takePhotoFromCamera error")
        }
        processorChain?.onFailed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CAPTURE_PHOTO) {
            return
        }

        if (resultCode != Activity.RESULT_OK) {
            processorChain?.onFailed()
        }

        // 检测图片是否被保存下来
        if (!File(savePath).exists()) {
            processorChain?.onFailed()
        } else {
            processorChain?.onResult(newUriList(savePath))
        }
    }

    companion object {
        private const val REQUEST_CAPTURE_PHOTO = 10901
    }

}