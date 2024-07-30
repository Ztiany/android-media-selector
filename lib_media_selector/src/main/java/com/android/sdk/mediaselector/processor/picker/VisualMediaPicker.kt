package com.android.sdk.mediaselector.processor.picker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.android.sdk.mediaselector.processor.BaseProcessor
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.utils.getClipDataUris
import com.android.sdk.mediaselector.utils.getSingleDataUri
import timber.log.Timber

/**
 * refers to [Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker).
 *
 * @see [androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia]
 * @see [androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia]
 */
internal class VisualMediaPicker(
    private val host: ActFragWrapper,
    private val type: ActivityResultContracts.PickVisualMedia.VisualMediaType,
    private val count: Int,
) : BaseProcessor() {

    override fun start(params: List<Uri>) {
        val intent = if (count == 1) {
            ActivityResultContracts.PickVisualMedia().createIntent(
                host.context,
                PickVisualMediaRequest.Builder().setMediaType(type).build()
            )
        } else {
            ActivityResultContracts.PickMultipleVisualMedia(count).createIntent(
                host.context,
                PickVisualMediaRequest.Builder().setMediaType(type).build()
            )
        }

        try {
            host.startActivityForResult(intent, REQUEST_VISUAL_PICKER)
        } catch (e: Exception) {
            processorChain.onFailed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_VISUAL_PICKER) {
            return
        }

        if (resultCode != Activity.RESULT_OK || data == null) {
            Timber.d("onActivityResult: canceled")
            processorChain.onCanceled()
            return
        }

        val result = if (count > 1) data.getClipDataUris() else data.getSingleDataUri()
        Timber.d("onActivityResult: result=$result")
        if (result.isEmpty()) {
            processorChain.onFailed()
        } else {
            processorChain.onResult(result.toList())
        }
    }

    companion object {
        private const val REQUEST_VISUAL_PICKER = 10905
    }

}