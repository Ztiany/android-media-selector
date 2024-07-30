package com.android.sdk.mediaselector.processor.crop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.getConfiguredPrimaryColor
import com.android.sdk.mediaselector.processor.BaseProcessor
import com.android.sdk.mediaselector.utils.createInternalVideoPath
import com.android.sdk.mediaselector.utils.getAbsolutePath
import com.android.sdk.mediaselector.utils.isCropSupported
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File

internal class CropProcessor(
    private val host: ActFragWrapper,
    private val cropOptions: CropOptions,
) : BaseProcessor() {

    private var originContent: List<Uri> = emptyList()

    private var processedContent = mutableListOf<Uri>()

    private var progress = 0

    override fun start(params: List<Uri>) {
        originContent = params
        progress = 0
        continueCropWork()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CROP) {
            return
        }
        if (resultCode != Activity.RESULT_OK || data == null) {
            processorChain.onCanceled()
            return
        }

        processCropResult(data)
    }

    private fun processCropResult(data: Intent) {
        val uCropResult = getUCropResult(data)
        Timber.d("processCropResult() called with: data = [$uCropResult]")
        if (uCropResult == null) {
            processorChain.onFailed()
            return
        }

        val absolutePath = uCropResult.getAbsolutePath(host.context)
        Timber.d("processCropResult() called with: absolutePath = [$absolutePath]")
        if (absolutePath.isNullOrEmpty()) {
            processorChain.onFailed()
            return
        }

        processedContent.add(Uri.fromFile(File(absolutePath)))
        continueCropWork()
    }

    private fun continueCropWork() {
        if (progress >= originContent.size) {
            processorChain.onResult(processedContent)
            return
        }

        val uriToCrop = originContent[progress]
        if (uriToCrop.isCropSupported(host.context)) {
            toUCrop(uriToCrop)
            progress++
        } else {
            processedContent.add(uriToCrop)
            progress++
            continueCropWork()
        }
    }

    private fun toUCrop(srcUri: Uri) {
        /*
        if src is a String.
         val srcUri = Uri.Builder()
            .scheme("file")
            .appendPath(srcPath)
            .build()
         */
        val targetPath: String = host.context.createInternalVideoPath()
        val targetUri = Uri.Builder()
            .scheme("file")
            .appendPath(targetPath)
            .build()

        // 参数
        val crop = UCrop.Options()
        crop.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        crop.withMaxResultSize(cropOptions.outputX, cropOptions.aspectY)
        crop.withAspectRatio(cropOptions.aspectX.toFloat(), cropOptions.aspectY.toFloat())

        // 颜色
        val color = host.context.getConfiguredPrimaryColor()
        crop.setToolbarColor(color)
        crop.setStatusBarColor(color)

        // 开始裁减
        if (host.fragment != null) {
            UCrop.of(srcUri, targetUri)
                .withOptions(crop)
                .start(host.context, host.fragment, REQUEST_CROP)
        } else if (host.activity != null) {
            UCrop.of(srcUri, targetUri)
                .withOptions(crop)
                .start(host.activity, REQUEST_CROP)
        }
    }

    private fun getUCropResult(data: Intent): Uri? {
        val throwable = UCrop.getError(data)
        if (throwable != null) {
            Timber.e(throwable, "getUCropResult")
            return null
        }
        return UCrop.getOutput(data)
    }

    companion object {
        private const val REQUEST_CROP = 10902
    }

}