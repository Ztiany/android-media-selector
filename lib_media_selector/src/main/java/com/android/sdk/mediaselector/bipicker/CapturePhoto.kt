package com.android.sdk.mediaselector.bipicker

import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.capture.CapturePhotoProcessor
import com.android.sdk.mediaselector.processor.crop.CropOptions
import com.android.sdk.mediaselector.processor.crop.CropProcessor
import com.android.sdk.mediaselector.utils.ActFragWrapper
import com.android.sdk.mediaselector.utils.StorageUtils

class CapturePhoto : Action {

    internal var builtInSelector: BuiltInSelectorImpl? = null

    private var cropOptions: CropOptions? = null
    private var savePath: String = ""

    fun crop(cropOptions: CropOptions = CropOptions()): CapturePhoto {
        this.cropOptions = cropOptions
        return this
    }

    fun saveTo(savePath: String): CapturePhoto {
        this.savePath = savePath
        return this
    }

    fun start() {
        builtInSelector?.start(this)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        if (savePath.isEmpty()) {
            savePath = StorageUtils.createInternalPicturePath(host.context, StorageUtils.JPEG)
        }

        return buildList {
            add(CapturePhotoProcessor(host, savePath))
            cropOptions?.let { add(CropProcessor(host, it)) }
        }
    }

}
