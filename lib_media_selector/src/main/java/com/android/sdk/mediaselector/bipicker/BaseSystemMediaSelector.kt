package com.android.sdk.mediaselector.bipicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.android.sdk.mediaselector.MediaSelectorConfiguration
import com.android.sdk.mediaselector.ResultListener
import com.android.sdk.mediaselector.utils.ActFragWrapper
import com.android.sdk.mediaselector.utils.MediaUtils
import com.android.sdk.mediaselector.utils.newUriList
import timber.log.Timber
import java.io.File

internal const val REQUEST_CAMERA = 10711
internal const val REQUEST_ALBUM = 10712
internal const val REQUEST_FILE = 10713
internal const val REQUEST_CROP = 10714

private const val INSTRUCTOR_KEY = "system_instructor_key"

/**
 * @author Ztiany
 */
internal abstract class BaseSystemMediaSelector : SystemMediaSelector {

    protected val mediaSelectorCallback: ResultListener

    private val actFragWrapper: ActFragWrapper

    protected lateinit var mCurrentInstruction: Instruction

    val context: Context
        get() = actFragWrapper.context

    protected val lifecycleOwner: LifecycleOwner

    constructor(activity: AppCompatActivity, resultListener: ResultListener) {
        actFragWrapper = ActFragWrapper.create(activity)
        lifecycleOwner = activity
        mediaSelectorCallback = resultListener
    }

    constructor(fragment: Fragment, resultListener: ResultListener) {
        mediaSelectorCallback = resultListener
        lifecycleOwner = fragment
        actFragWrapper = ActFragWrapper.create(fragment)
    }

    protected fun startActivityForResult(intent: Intent, code: Int) {
        actFragWrapper.startActivityForResult(intent, code, null)
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        if (::mCurrentInstruction.isInitialized) {
            outState.putParcelable(INSTRUCTOR_KEY, mCurrentInstruction)
        }
    }

    @CallSuper
    override fun onRestoreInstanceState(outState: Bundle?) {
        if (!::mCurrentInstruction.isInitialized) {
            outState?.getParcelable<Instruction>(INSTRUCTOR_KEY)?.let {
                mCurrentInstruction = it
            }
        }
        if (::mCurrentInstruction.isInitialized) {
            mCurrentInstruction.setMediaSelector(this)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Result
    ///////////////////////////////////////////////////////////////////////////

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CAMERA
            && requestCode != REQUEST_ALBUM
            && requestCode != REQUEST_FILE
            && requestCode != REQUEST_CROP) {
            return
        }

        Timber.d("onActivityResult() called with: requestCode = [$requestCode], resultCode = [$resultCode], data = [$data]")
        if (resultCode != Activity.RESULT_OK) {
            mediaSelectorCallback.onCanceled()
            return
        }

        when (requestCode) {
            REQUEST_CAMERA -> processCameraResult()
            REQUEST_ALBUM -> processSystemPhotoResult(data)
            REQUEST_FILE -> processFileResult(data)
            REQUEST_CROP -> processCropResult(data)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Crop
    ///////////////////////////////////////////////////////////////////////////

    protected fun toCrop(src: String) {
        MediaUtils.toUCrop(
            actFragWrapper,
            src,
            mCurrentInstruction.cropOptions,
            REQUEST_CROP
        )
    }

    private fun processCropResult(data: Intent?) {
        val uCropResult = MediaUtils.getUCropResult(data)
        Timber.d("processCropResult() called with: resultCode = [], data = [$uCropResult]")
        if (uCropResult == null) {
            mediaSelectorCallback.onFailed()
        } else {
            val absolutePath = MediaUtils.getAbsolutePath(context, uCropResult)
            if (!TextUtils.isEmpty(absolutePath)) {
                mediaSelectorCallback.onResult(newUriList(absolutePath))
            } else {
                mediaSelectorCallback.onFailed()
            }
            Timber.d("processCropResult() called with: resultCode = [], data = [$absolutePath]")
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Camera
    ///////////////////////////////////////////////////////////////////////////

    override fun takePhotoByCamera(): Instruction {
        return Instruction(this, Instruction.CAMERA)
    }

    fun takePhotoFromCamera(instruction: Instruction): Boolean {
        mCurrentInstruction = instruction
        if (!MediaUtils.hasCamera(context)) {
            Timber.w("The device has no camera apps.")
            return false
        }
        val targetFile = File(instruction.cameraPhotoSavePath)
        val intent = MediaUtils.makeCaptureIntent(context, targetFile, MediaSelectorConfiguration.getAuthority(context))
        try {
            startActivityForResult(intent, REQUEST_CAMERA)
            return true
        } catch (e: Exception) {
            Timber.e(e, "takePhotoFromCamera error")
        }
        return false
    }

    private fun processCameraResult() {
        //需要裁减，可以裁减则进行裁减，否则直接返回
        if (mCurrentInstruction.needCrop()) {
            //检测图片是否被保存下来
            val photoPath = File(mCurrentInstruction.cameraPhotoSavePath)
            if (!photoPath.exists()) {
                mediaSelectorCallback.onFailed()
                return
            }
            val cameraPhotoSavePath = mCurrentInstruction.cameraPhotoSavePath
            toCrop(cameraPhotoSavePath)
            return
        }

        //检测图片是否被保存下来
        if (!File(mCurrentInstruction.cameraPhotoSavePath).exists()) {
            mediaSelectorCallback.onFailed()
        } else {
            mediaSelectorCallback.onResult(newUriList(mCurrentInstruction.cameraPhotoSavePath))
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Album
    ///////////////////////////////////////////////////////////////////////////

    override fun takePhotoFromSystem(): Instruction {
        return Instruction(this, Instruction.PHOTO)
    }

    fun takePhotoFormSystem(instruction: Instruction): Boolean {
        mCurrentInstruction = instruction
        return doTakePhotoFormSystem()
    }

    protected abstract fun doTakePhotoFormSystem(): Boolean

    protected abstract fun processSystemPhotoResult(data: Intent?)

    ///////////////////////////////////////////////////////////////////////////
    // File
    ///////////////////////////////////////////////////////////////////////////

    override fun takeFileFromSystem(): Instruction {
        return Instruction(this, Instruction.FILE)
    }

    fun takeFile(instruction: Instruction): Boolean {
        mCurrentInstruction = instruction
        return doTakeFile()
    }

    protected abstract fun doTakeFile(): Boolean

    protected abstract fun processFileResult(data: Intent?)

}