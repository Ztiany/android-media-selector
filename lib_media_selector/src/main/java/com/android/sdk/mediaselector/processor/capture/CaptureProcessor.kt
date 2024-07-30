package com.android.sdk.mediaselector.processor.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.getConfiguredAuthority
import com.android.sdk.mediaselector.processor.BaseProcessor
import com.android.sdk.mediaselector.utils.makeFilePath
import timber.log.Timber
import java.io.File

/**
 * @see [androidx.activity.result.contract.ActivityResultContracts.TakePicture]
 * @see [androidx.activity.result.contract.ActivityResultContracts.CaptureVideo]
 */
internal class CaptureProcessor(
    private val host: ActFragWrapper,
    private val type: Int,
    private val savePath: String,
) : BaseProcessor() {

    override fun start(params: List<Uri>) {
        Timber.d("start is called with: $params")
        if (!hasCamera(host.context)) {
            Timber.w("The device has no camera apps.")
            processorChain.onFailed()
        }

        val intent = makeCaptureIntent(host.context, File(savePath), getConfiguredAuthority(host.context))
        try {
            host.startActivityForResult(intent, REQUEST_CAPTURE_PHOTO, null)
        } catch (e: Exception) {
            Timber.e(e, "takePhotoFromCamera error")
            processorChain.onFailed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CAPTURE_PHOTO) {
            return
        }

        if (resultCode != Activity.RESULT_OK) {
            processorChain.onFailed()
        }

        // 检测文件是否被保存下来
        val savedFile = File(savePath)
        if (!savedFile.exists()) {
            processorChain.onFailed()
        } else {
            processorChain.onResult(listOf(Uri.fromFile(savedFile)))
        }
    }

    /**
     * 判断系统中是否存在可以启动的相机应用。
     *
     * @return 存在返回 true，不存在返回 false。
     */
    private fun hasCamera(context: Context): Boolean {
        val packageManager = context.packageManager
        val intent = if (type == IMAGE) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        } else {
            Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        }
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
    }

    /**
     * @param targetFile 源文件，裁剪之后新的图片覆盖此文件。
     */
    private fun makeCaptureIntent(context: Context, targetFile: File, authority: String): Intent {
        targetFile.makeFilePath()
        val intent = if (type == IMAGE) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        } else {
            Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        }
        if (Build.VERSION.SDK_INT < 24) {
            val fileUri = Uri.fromFile(targetFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        } else {
            val fileUri = FileProvider.getUriForFile(context, authority, targetFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return intent
    }

    companion object {
        private const val REQUEST_CAPTURE_PHOTO = 10901

        const val IMAGE = 1
        const val VIDEO = 2
    }

}