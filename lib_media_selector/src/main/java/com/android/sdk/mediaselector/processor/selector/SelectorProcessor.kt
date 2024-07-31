package com.android.sdk.mediaselector.processor.selector

import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.Item
import com.android.sdk.mediaselector.imageloader.GlideEngine
import com.android.sdk.mediaselector.processor.BaseProcessor
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener

internal class SelectorProcessor(
    private val host: ActFragWrapper,
    private val selectorConfig: SelectorConfig,
) : BaseProcessor() {

    override fun start(params: List<Item>) {
        start()
        /*
        we don't need it, because PictureSelector will handle the Dynamic permissions itself.
        if (host.fragmentActivity.getPermissionState() != MediaPermissionState.None) {
            start()
        } else {
            getPermissionRequester().askForMediaPermissionWhenUsingMediaStoreAPI(
                host.fragmentActivity,
                onGranted = { _, _ -> start() },
                onDenied = { processorChain.onCanceled() }
            )
        }*/
    }

    private fun start() {
        if (host.activity != null) {
            PictureSelector.create(host.activity)
        } else {
            PictureSelector.create(host.fragment)
        }
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    handleResult(result)
                }

                override fun onCancel() {
                    processorChain.onCanceled()
                }
            })
    }

    private fun handleResult(result: java.util.ArrayList<LocalMedia>) {

    }

    /*private fun BaseMedia.toItem(): Item {
        if (this is ImageMedia) {
            return Item(
                id = id,
                rawUri = uri,
                uri = uri,
                mineType = mimeType,
                rawWidth = width,
                rawHeight = height,
                rawSize = size
            )
        }

        if (this is VideoMedia) {
            return Item(
                id = id,
                rawUri = uri,
                uri = uri,
                mineType = mimeType,
                rawWidth = 0,
                rawHeight = 0,
                rawSize = size,
                duration = 0
            )
        }
        throw IllegalStateException("impossible")
    }*/

}