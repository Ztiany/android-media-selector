package com.android.sdk.mediaselector.actions

import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.ParcelCompat
import com.android.sdk.mediaselector.Action
import com.android.sdk.mediaselector.MediaSelectorImpl
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.crop.CropOptions
import com.android.sdk.mediaselector.processor.crop.CropProcessor
import com.android.sdk.mediaselector.processor.picker.SAFPicker
import com.android.sdk.mediaselector.processor.picker.VisualMediaPicker
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.utils.MineType

class ImagePicker() : Action {

    internal var builtInSelector: MediaSelectorImpl? = null

    private var count: Int = 1

    private var cropOptions: CropOptions? = null

    fun count(count: Int): ImagePicker {
        this.count = count
        return this
    }

    fun crop(cropOptions: CropOptions = CropOptions()): ImagePicker {
        this.cropOptions = cropOptions
        return this
    }

    fun start() {
        builtInSelector?.start(this)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        return buildList {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(host.context)) {
                add(VisualMediaPicker(host, ActivityResultContracts.PickVisualMedia.ImageOnly, count))
            } else {
                add(SAFPicker(host, listOf(MineType.IMAGE.value), count > 1))
            }
            cropOptions?.let { add(CropProcessor(host, it)) }
        }
    }

    constructor(parcel: Parcel) : this() {
        count = parcel.readInt()
        cropOptions = ParcelCompat.readParcelable(parcel, CropOptions::class.java.classLoader, CropOptions::class.java)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeParcelable(cropOptions, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImagePicker> {
        override fun createFromParcel(parcel: Parcel): ImagePicker {
            return ImagePicker(parcel)
        }

        override fun newArray(size: Int): Array<ImagePicker?> {
            return arrayOfNulls(size)
        }
    }

}