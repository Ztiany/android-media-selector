package com.android.sdk.mediaselector.mediastore

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.Action
import com.android.sdk.mediaselector.AllInOneImplementation
import com.android.sdk.mediaselector.R
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.boxing.BoxingProcessor
import com.android.sdk.mediaselector.processor.crop.CropOptions
import com.android.sdk.mediaselector.processor.crop.CropProcessor
import com.bilibili.boxing.model.callback.MediaFilter
import com.bilibili.boxing.model.config.BoxingConfig
import timber.log.Timber

class SelectImageAction() : Action {

    internal var builtInSelector: AllInOneImplementation? = null

    private var cropOptions: CropOptions? = null
    private var mediaFilter: MediaFilter? = null
    private var count = 1
    private var needGif = false
    private var showCamera = false
    private var acquireLocation = false

    fun crop(cropOptions: CropOptions = CropOptions()): SelectImageAction {
        this.cropOptions = cropOptions
        return this
    }

    fun filter(mediaFilter: MediaFilter): SelectImageAction {
        this.mediaFilter = mediaFilter
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

    fun acquireLocation(): SelectImageAction {
        this.acquireLocation = true
        return this
    }

    fun start() {
        builtInSelector?.start(this)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        return buildList {
            add(BoxingProcessor(host, acquireLocation, createBoxingConfig()))
            cropOptions?.let { add(CropProcessor(host, it)) }
        }
    }

    private fun createBoxingConfig(): BoxingConfig {
        return if (count > 1) {
            Timber.d("selecting multi pictures: %d", count)
            BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withMaxCount(count)
        } else {
            Timber.d("selecting single picture")
            BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
        }.also {
            it.mediaFilter = mediaFilter
            if (needGif) {
                Timber.d("need gif")
                it.needGif()
            }
            if (showCamera) {
                it.needCamera(R.drawable.ic_boxing_camera)
            }
        }
    }

    constructor(parcel: Parcel) : this() {
        cropOptions = ParcelCompat.readParcelable(parcel, CropOptions::class.java.classLoader, CropOptions::class.java)
        mediaFilter = ParcelCompat.readParcelable(parcel, MediaFilter::class.java.classLoader, MediaFilter::class.java)
        count = parcel.readInt()
        needGif = parcel.readByte() != 0.toByte()
        showCamera = parcel.readByte() != 0.toByte()
        acquireLocation = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(cropOptions, flags)
        parcel.writeParcelable(mediaFilter, flags)
        parcel.writeInt(count)
        parcel.writeByte(if (needGif) 1 else 0)
        parcel.writeByte(if (showCamera) 1 else 0)
        parcel.writeByte(if (acquireLocation) 1 else 0)
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