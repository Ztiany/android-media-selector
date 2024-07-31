package com.android.sdk.mediaselector.actions

import android.os.Parcel
import android.os.Parcelable
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.Action
import com.android.sdk.mediaselector.MediaSelectorImpl
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.selector.SelectorConfig
import com.android.sdk.mediaselector.processor.selector.SelectorProcessor

class SelectVideoAction() : Action {

    internal var builtInSelector: MediaSelectorImpl? = null

    private var count = 1
    private var showCamera = false


    fun count(count: Int): SelectVideoAction {
        this.count = count
        return this
    }

    fun showCamera(): SelectVideoAction {
        this.showCamera = true
        return this
    }

    fun start() {
        builtInSelector?.start(this)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        return buildList {
            add(SelectorProcessor(host, createConfig()))
        }
    }

    private fun createConfig(): SelectorConfig {
        return SelectorConfig()
    }

    constructor(parcel: Parcel) : this() {
        count = parcel.readInt()
        showCamera = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeByte(if (showCamera) 1 else 0)
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