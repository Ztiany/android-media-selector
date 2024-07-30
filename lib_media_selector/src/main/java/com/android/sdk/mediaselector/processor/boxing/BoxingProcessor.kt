package com.android.sdk.mediaselector.processor.boxing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.imageloader.BoxingGlideLoader
import com.android.sdk.mediaselector.initializeBoxIfNeed
import com.android.sdk.mediaselector.processor.BaseProcessor
import com.android.sdk.mediaselector.utils.setRequireOriginal
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing_impl.ui.BoxingActivity
import timber.log.Timber

internal class BoxingProcessor(
    private val host: ActFragWrapper,
    private val accessLocation: Boolean,
    private val boxingConfig: BoxingConfig,
) : BaseProcessor() {

    override fun start(params: List<Uri>) {
        start(boxingConfig)
    }

    private fun start(boxingConfig: BoxingConfig) {
        val fragment = host.fragment
        try {
            if (fragment != null) {
                val boxing = Boxing.of(boxingConfig).withIntent(fragment.requireContext(), BoxingActivity::class.java)
                boxing.start(fragment, REQUEST_BOXING)
            } else {
                val boxing = Boxing.of(boxingConfig).withIntent(host.context, BoxingActivity::class.java)
                boxing.start(host.context as Activity, REQUEST_BOXING)
            }
        } catch (e: Exception) {
            Timber.e(e, "start(boxingConfig) ")
            processorChain.onFailed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_BOXING) {
            return
        }
        if (resultCode != Activity.RESULT_OK || data == null) {
            processorChain.onCanceled()
            return
        }
        handleResult(data)
    }

    private fun handleResult(data: Intent) {
        val medias = Boxing.getResult(data)
        if (medias.isNullOrEmpty()) {
            processorChain.onCanceled()
            return
        }

        processorChain.onResult(medias.map {
            if (accessLocation) {
                it.uri.setRequireOriginal(host.context)
            }
            it.uri
        })
    }

    companion object {
        private const val REQUEST_BOXING = 10906

        init {
            initializeBoxIfNeed()
        }

    }

}