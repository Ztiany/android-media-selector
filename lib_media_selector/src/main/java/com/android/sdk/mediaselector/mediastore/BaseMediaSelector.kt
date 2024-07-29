package com.android.sdk.mediaselector.mediastore

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.android.sdk.mediaselector.utils.ActFragWrapper
import com.android.sdk.mediaselector.utils.MediaUtils
import com.android.sdk.mediaselector.ResultListener
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing_impl.ui.BoxingActivity
import com.android.sdk.mediaselector.R
import timber.log.Timber

private const val REQUEST_BOXING = 10715;
internal const val REQUEST_CROP = 10716
private const val INSTRUCTOR_KEY = "custom_instructor_key"

/**
 *@author Ztiany
 */
internal abstract class BaseMediaSelector : MediaSelector {

    private val actFragWrapper: ActFragWrapper

    protected val mediaSelectorCallback: ResultListener

    protected val lifecycleOwner: LifecycleOwner

    protected lateinit var currentInstruction: Instruction

    val context: Context
        get() = actFragWrapper.context

    constructor(activity: AppCompatActivity, resultListener: ResultListener) {
        actFragWrapper = ActFragWrapper.create(activity)
        mediaSelectorCallback = resultListener
        lifecycleOwner = activity
    }

    constructor(fragment: Fragment, resultListener: ResultListener) {
        mediaSelectorCallback = resultListener
        actFragWrapper = ActFragWrapper.create(fragment)
        lifecycleOwner = fragment
    }


    final override fun takePicture(): Instruction {
        return Instruction(this, Instruction.PICTURE)
    }

    final override fun takeVideo(): Instruction {
        return Instruction(this, Instruction.VIDEO)
    }

    fun start(instruction: Instruction): Boolean {
        currentInstruction = instruction
        return configAndStart()
    }

    private fun configAndStart(): Boolean {
        val boxingConfig = createBoxingConfig()
        if (currentInstruction.isNeedCamera) {
            boxingConfig.needCamera(R.drawable.ic_boxing_camera)
        }
        return start(boxingConfig)
    }

    private fun start(boxingConfig: BoxingConfig): Boolean {
        Timber.d(" -start(boxingConfig)")
        val fragment = actFragWrapper.fragment
        return try {
            if (fragment != null) {
                val boxing = Boxing.of(boxingConfig).withIntent(fragment.requireContext(), BoxingActivity::class.java)
                boxing.start(fragment, REQUEST_BOXING)
            } else {
                val boxing = Boxing.of(boxingConfig).withIntent(actFragWrapper.context, BoxingActivity::class.java)
                boxing.start(actFragWrapper.context as Activity, REQUEST_BOXING)
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "start(boxingConfig) ")
            e.printStackTrace()
            false
        }
    }

    private fun createBoxingConfig(): BoxingConfig {
        return if (currentInstruction.takingType == Instruction.PICTURE) {
            if (currentInstruction.moreThanOne()) {
                Timber.d("selecting multi pictures: %d", currentInstruction.count)
                BoxingConfig(BoxingConfig.Mode.MULTI_IMG).withMaxCount(currentInstruction.count)
            } else {
                Timber.d("selecting single picture")
                BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
            }.also {
                if (currentInstruction.isNeedGif) {
                    Timber.d("need gif")
                    it.needGif()
                }
            }
        } else {
            Timber.d("selecting single video")
            BoxingConfig(BoxingConfig.Mode.VIDEO)
        }.also {
            it.mediaFilter = currentInstruction.mediaFilter
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Result
    ///////////////////////////////////////////////////////////////////////////

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult() called with: requestCode = [$requestCode], resultCode = [$resultCode], data = [$data]")
        if (requestCode != REQUEST_BOXING && requestCode != REQUEST_CROP) {
            return
        }

        if (resultCode != Activity.RESULT_OK) {
            mediaSelectorCallback.onCanceled()
            return
        }
        if (data == null) {
            mediaSelectorCallback.onCanceled()
            return
        }

        if (requestCode == REQUEST_BOXING) {
            handleResult(data)
        } else if (requestCode == REQUEST_CROP) {
            processCropResult(data)
        }
    }

    private fun handleResult(data: Intent) {
        val medias = Boxing.getResult(data)
        if (medias.isNullOrEmpty()) {
            mediaSelectorCallback.onCanceled()
            return
        }

        if (medias.size > 1) {
            handleMultiResult(medias)
        } else {
            handleSingleResult(medias[0])
        }
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        if (::currentInstruction.isInitialized) {
            outState.putParcelable(INSTRUCTOR_KEY, currentInstruction)
        }
    }

    @CallSuper
    override fun onRestoreInstanceState(outState: Bundle?) {
        if (!::currentInstruction.isInitialized) {
            outState?.getParcelable<Instruction>(INSTRUCTOR_KEY)?.let {
                currentInstruction = it
            }
        }
        if (::currentInstruction.isInitialized) {
            currentInstruction.setMediaSelector(this)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Crop
    ///////////////////////////////////////////////////////////////////////////

    protected fun toCrop(src: String) {
        MediaUtils.toUCrop(
            actFragWrapper,
            src,
            currentInstruction.cropOptions,
            REQUEST_CROP
        )
    }

    private fun processCropResult(data: Intent?) {
        val uCropResult = MediaUtils.getUCropResult(data)
        if (uCropResult == null) {
            mediaSelectorCallback.onFailed()
        } else {
            val absolutePath = MediaUtils.getAbsolutePath(context, uCropResult)
            if (!TextUtils.isEmpty(absolutePath)) {
                handleSingleCropResult(absolutePath)
            } else {
                mediaSelectorCallback.onFailed()
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // for children
    ///////////////////////////////////////////////////////////////////////////

    abstract fun handleSingleResult(baseMedia: BaseMedia)
    abstract fun handleSingleCropResult(absolutePath: String)
    abstract fun handleMultiResult(medias: ArrayList<BaseMedia>)

}