package com.android.sdk.mediaselector.bipicker

import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.crop.CropOptions
import com.android.sdk.mediaselector.processor.crop.CropProcessor
import com.android.sdk.mediaselector.utils.ActFragWrapper

class PickPhoto : Action {

    internal var builtInSelector: BuiltInSelectorImpl? = null
    
    private var count: Int = 1

    private var cropOptions: CropOptions? = null

    fun count(count: Int): PickPhoto {
        this.count = count
        return this
    }

    fun crop(cropOptions: CropOptions = CropOptions()): PickPhoto {
        this.cropOptions = cropOptions
        return this
    }
    
    fun start() {
        builtInSelector?.start(this)
    }
    
    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        return buildList {
            cropOptions?.let { add(CropProcessor(host, it)) }
        }
    }

}