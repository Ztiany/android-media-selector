package com.android.sdk.mediaselector.processor.selector

import android.net.Uri
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.Item
import com.android.sdk.mediaselector.imageloader.GlideEngine
import com.android.sdk.mediaselector.processor.BaseProcessor
import com.android.sdk.mediaselector.processor.compress.ImageFileCompressEngine
import com.google.gson.Gson
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.yalantis.ucrop.UCropImageEngine
import timber.log.Timber

internal class SelectorProcessor(
    private val host: ActFragWrapper,
    private val selectorConfig: SelectorConfig,
) : BaseProcessor() {

    override fun start(params: List<Item>) {
        start()
        /*
        we don't need it, because PictureSelector will handle the Dynamic permissions itself.
        if (host.fragmentActivity.getPermissionState() != MediaPermissionState.None) {
            start()
        } else {
            getPermissionRequester().askForMediaPermissionWhenUsingMediaStoreAPI(
                host.fragmentActivity,
                onGranted = { _, _ -> start() },
                onDenied = { processorChain.onCanceled() }
            )
        }*/
    }

    private fun start() {
        if (host.activity != null) {
            PictureSelector.create(host.activity)
        } else {
            PictureSelector.create(host.fragment)
        }
//            .openGallery(SelectMimeType.ofAll())

            .openSystemGallery(SelectMimeType.ofAll())
            //.setMaxSelectNum(9)
            //.setImageEngine(GlideEngine.createGlideEngine())
            //.isWithSelectVideoImage(true)
            /* .isBmp(true)
             .isGif(true)
             .isWebp(true)
             .isHeic(true)*/
            //.isDisplayTimeAxis(true)
            //.isDisplayCamera(false)
            .setSelectMaxFileSize(100000)
            .setSelectMinFileSize(100)
            .setSelectMaxDurationSecond(100)
            .setSelectMinDurationSecond(1)
//            .setCropEngine(ImageFileCropEngine())
            .setCompressEngine(ImageFileCompressEngine())
            .forSystemResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    handleResult(result)
                }

                override fun onCancel() {
                    processorChain.onCanceled()
                }
            })
            //.isPreloadFirst(true)
            //.setSelectedData(emptyList())
            /*.forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    handleResult(result)
                }

                override fun onCancel() {
                    processorChain.onCanceled()
                }
            })*/
    }

    /*
    {
    "bucketId": -1739773001,
    "chooseModel": 0,
    "compressPath": "/storage/emulated/0/Android/data/me.ztiany.midea.selector.example/files/Pictures/CROP_20240731211548672.jpg",
    "compressed": true,
    "cropImageHeight": 454,
    "cropImageWidth": 808,
    "cropOffsetX": 775,
    "cropOffsetY": 372,
    "cropResultAspectRatio": 1.7785715,
    "cutPath": "/storage/emulated/0/Android/data/me.ztiany.midea.selector.example/files/Pictures/CROP_20240731211548672.jpg",
    "dateAddedTime": 1716817126,
    "duration": 0,
    "fileName": "IMG_20240527_173846.jpg",
    "height": 1728,
    "id": 1000057950,
    "isCameraSource": false,
    "isChecked": false,
    "isCut": true,
    "isEditorImage": false,
    "isGalleryEnabledMask": false,
    "isMaxSelectEnabledMask": false,
    "isOriginal": false,
    "mimeType": "image/jpeg",
    "num": 0,
    "parentFolderName": "Camera",
    "path": "content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20240527_173846.jpg",
    "position": 0,
    "realPath": "/storage/emulated/0/DCIM/Camera/IMG_20240527_173846.jpg",
    "sandboxPath": "/storage/emulated/0/Android/data/me.ztiany.midea.selector.example/files/Pictures/CROP_20240731211548672.jpg",
    // 原始 size
    "size": 1411098,
    "width": 3072
}
     */
    private fun handleResult(result: ArrayList<LocalMedia>) {
        result.forEach {
            Timber.d("handleResult: ${Gson().toJson(it)}")
        }
        processorChain.onResult(result.map { it.toItem() })
    }

    private fun LocalMedia.toItem(): Item {
        val rawUri = Uri.parse(path)
        return Item(
            id = id.toString(),
            mineType = mimeType,
            duration = duration,
            // final
            uri = rawUri,
            path = path,
            width = width,
            height = height,
            size = size,
            // raw
            rawUri = rawUri,
            rawPath = path,
            rawSize = size,
            rawWidth = width,
            rawHeight = height,
        )
    }

    /*private fun BaseMedia.toItem(): Item {
        if (this is ImageMedia) {
            return Item(
                id = id,
                rawUri = uri,
                uri = uri,
                mineType = mimeType,
                rawWidth = width,
                rawHeight = height,
                rawSize = size
            )
        }

        if (this is VideoMedia) {
            return Item(
                id = id,
                rawUri = uri,
                uri = uri,
                mineType = mimeType,
                rawWidth = 0,
                rawHeight = 0,
                rawSize = size,
                duration = 0
            )
        }
        throw IllegalStateException("impossible")
    }*/

}
