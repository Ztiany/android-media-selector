package com.android.sdk.mediaselector.actions

import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.ParcelCompat
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.Action
import com.android.sdk.mediaselector.MediaSelectorImpl
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.crop.CropOptions
import com.android.sdk.mediaselector.processor.crop.CropProcessor
import com.android.sdk.mediaselector.processor.picker.SAFPicker
import com.android.sdk.mediaselector.processor.picker.VisualMediaPicker
import com.android.sdk.mediaselector.utils.MineType

class ImagePicker() : Action {

    internal var builtInSelector: MediaSelectorImpl? = null

    private var count: Int = 1
    private var type = ""

    private var cropOptions: CropOptions? = null

    private var useSAF = false
    private var takePersistentUriPermission = false

    fun count(count: Int): ImagePicker {
        this.count = count
        return this
    }

    fun crop(cropOptions: CropOptions = CropOptions()): ImagePicker {
        this.cropOptions = cropOptions
        return this
    }

    fun restrictTypeTo(type: String): ImagePicker {
        if (!type.startsWith("image/")) {
            throw IllegalArgumentException("Type must be a video mime type")
        }
        this.type = type
        return this
    }

    override fun start(scene: String) {
        builtInSelector?.start(this, scene)
    }

    override fun assembleProcessors(host: ActFragWrapper): List<Processor> {
        return buildList {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(host.context) && !useSAF && !takePersistentUriPermission) {
                val visualType = if (type.isEmpty()) {
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                } else ActivityResultContracts.PickVisualMedia.SingleMimeType(type)
                add(VisualMediaPicker(host, visualType, count))
            } else {
                add(SAFPicker(host, listOf(type.takeIf { it.isNotEmpty() } ?: MineType.IMAGE.value), takePersistentUriPermission, count > 1))
            }
            cropOptions?.let { add(CropProcessor(host, it)) }
        }
    }

    constructor(parcel: Parcel) : this() {
        count = parcel.readInt()
        type = parcel.readString() ?: ""
        cropOptions = ParcelCompat.readParcelable(parcel, CropOptions::class.java.classLoader, CropOptions::class.java)
        useSAF = parcel.readByte() != 0.toByte()
        takePersistentUriPermission = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeString(type)
        parcel.writeParcelable(cropOptions, flags)
        parcel.writeByte(if (useSAF) 1 else 0)
        parcel.writeByte(if (takePersistentUriPermission) 1 else 0)
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