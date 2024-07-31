package com.android.sdk.mediaselector.actions

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.Action
import com.android.sdk.mediaselector.MediaSelectorImpl
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.crop.CropOptions
import com.android.sdk.mediaselector.processor.crop.CropProcessor
import com.android.sdk.mediaselector.processor.selector.SelectorConfig
import com.android.sdk.mediaselector.processor.selector.SelectorProcessor

class SelectImageAction() : Action {

    internal var builtInSelector: MediaSelectorImpl? = null

    private var cropOptions: CropOptions? = null
    private var count = 1
    private var needGif = false
    private var showCamera = false

    fun crop(cropOptions: CropOptions = CropOptions()): SelectImageAction {
        this.cropOptions = cropOptions
        return this
    }


    fun count(count: Int): SelectImageAction {
        this.count = count
        return this
    }

    fun includeGif(): SelectImageAction {
        this.needGif = true
        return this
    }

    fun enableCamera(): SelectImageAction {
        this.showCamera = true
        return this
    }

    fun start() {
        builtInSelector?.start(this)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        return buildList {
            add(SelectorProcessor(host, createConfig()))
            cropOptions?.let { add(CropProcessor(host, it)) }
        }
    }

    private fun createConfig(): SelectorConfig {
        return SelectorConfig()
    }

    constructor(parcel: Parcel) : this() {
        cropOptions = ParcelCompat.readParcelable(parcel, CropOptions::class.java.classLoader, CropOptions::class.java)
        count = parcel.readInt()
        needGif = parcel.readByte() != 0.toByte()
        showCamera = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(cropOptions, flags)
        parcel.writeInt(count)
        parcel.writeByte(if (needGif) 1 else 0)
        parcel.writeByte(if (showCamera) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SelectImageAction> {
        override fun createFromParcel(parcel: Parcel): SelectImageAction {
            return SelectImageAction(parcel)
        }

        override fun newArray(size: Int): Array<SelectImageAction?> {
            return arrayOfNulls(size)
        }
    }

}