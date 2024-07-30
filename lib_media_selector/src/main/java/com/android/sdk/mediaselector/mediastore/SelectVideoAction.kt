package com.android.sdk.mediaselector.mediastore

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import com.android.sdk.mediaselector.Action
import com.android.sdk.mediaselector.AllInOneImplementation
import com.android.sdk.mediaselector.R
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.boxing.BoxingProcessor
import com.android.sdk.mediaselector.ActFragWrapper
import com.bilibili.boxing.model.callback.MediaFilter
import com.bilibili.boxing.model.config.BoxingConfig

class SelectVideoAction() : Action {

    internal var builtInSelector: AllInOneImplementation? = null

    private var mediaFilter: MediaFilter? = null
    private var count = 1
    private var showCamera = false
    private var acquireLocation = false

    fun filter(mediaFilter: MediaFilter): SelectVideoAction {
        this.mediaFilter = mediaFilter
        return this
    }

    fun count(count: Int): SelectVideoAction {
        this.count = count
        return this
    }

    fun showCamera(): SelectVideoAction {
        this.showCamera = true
        return this
    }

    fun acquireLocation(): SelectVideoAction {
        this.acquireLocation = true
        return this
    }

    fun start() {
        builtInSelector?.start(this)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        return buildList {
            add(BoxingProcessor(host, acquireLocation, createBoxingConfig()))
        }
    }

    private fun createBoxingConfig(): BoxingConfig {
        return BoxingConfig(BoxingConfig.Mode.VIDEO).also {
            it.mediaFilter = mediaFilter
            if (showCamera) {
                it.needCamera(R.drawable.ic_boxing_camera)
            }
        }
    }

    constructor(parcel: Parcel) : this() {
        mediaFilter = ParcelCompat.readParcelable(parcel, MediaFilter::class.java.classLoader, MediaFilter::class.java)
        count = parcel.readInt()
        showCamera = parcel.readByte() != 0.toByte()
        acquireLocation = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(mediaFilter, flags)
        parcel.writeInt(count)
        parcel.writeByte(if (showCamera) 1 else 0)
        parcel.writeByte(if (acquireLocation) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SelectVideoAction> {
        override fun createFromParcel(parcel: Parcel): SelectVideoAction {
            return SelectVideoAction(parcel)
        }

        override fun newArray(size: Int): Array<SelectVideoAction?> {
            return arrayOfNulls(size)
        }
    }

}