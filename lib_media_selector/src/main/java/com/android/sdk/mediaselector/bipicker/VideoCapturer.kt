package com.android.sdk.mediaselector.bipicker

import android.os.Parcel
import android.os.Parcelable
import com.android.sdk.mediaselector.Action
import com.android.sdk.mediaselector.AllInOneImplementation
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.capture.CaptureProcessor
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.utils.createInternalVideoPath

class VideoCapturer() : Action {

    internal var builtInSelector: AllInOneImplementation? = null

    private var savePath: String = ""

    fun saveTo(savePath: String): VideoCapturer {
        this.savePath = savePath
        return this
    }

    fun start() {
        builtInSelector?.start(this)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        if (savePath.isEmpty()) {
            savePath = host.context.createInternalVideoPath()
        }
        return buildList {
            add(CaptureProcessor(host, CaptureProcessor.VIDEO, savePath))
        }
    }

    constructor(parcel: Parcel) : this() {
        savePath = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(savePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoCapturer> {
        override fun createFromParcel(parcel: Parcel): VideoCapturer {
            return VideoCapturer(parcel)
        }

        override fun newArray(size: Int): Array<VideoCapturer?> {
            return arrayOfNulls(size)
        }
    }

}