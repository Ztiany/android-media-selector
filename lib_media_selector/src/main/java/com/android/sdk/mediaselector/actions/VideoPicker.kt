package com.android.sdk.mediaselector.actions

import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import com.android.sdk.mediaselector.Action
import com.android.sdk.mediaselector.MediaSelectorImpl
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.picker.SAFPicker
import com.android.sdk.mediaselector.processor.picker.VisualMediaPicker
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.utils.MineType

class VideoPicker() : Action {

    internal var builtInSelector: MediaSelectorImpl? = null

    private var count: Int = 1

    fun count(count: Int): VideoPicker {
        this.count = count
        return this
    }

    fun start() {
        builtInSelector?.start(this)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        return buildList {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(host.context)) {
                add(VisualMediaPicker(host, ActivityResultContracts.PickVisualMedia.VideoOnly, count))
            } else {
                add(SAFPicker(host, listOf(MineType.VIDEO.value), count > 1))
            }
        }
    }

    constructor(parcel: Parcel) : this() {
        count = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoPicker> {
        override fun createFromParcel(parcel: Parcel): VideoPicker {
            return VideoPicker(parcel)
        }

        override fun newArray(size: Int): Array<VideoPicker?> {
            return arrayOfNulls(size)
        }
    }

}