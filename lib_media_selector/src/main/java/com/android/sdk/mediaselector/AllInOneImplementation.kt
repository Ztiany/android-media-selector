package com.android.sdk.mediaselector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.android.sdk.mediaselector.bipicker.BuiltInPicker
import com.android.sdk.mediaselector.bipicker.FilePicker
import com.android.sdk.mediaselector.bipicker.GetContent
import com.android.sdk.mediaselector.bipicker.GetImageContent
import com.android.sdk.mediaselector.bipicker.GetVideoContent
import com.android.sdk.mediaselector.bipicker.ImageAndVideoPicker
import com.android.sdk.mediaselector.bipicker.ImageCapturer
import com.android.sdk.mediaselector.bipicker.ImagePicker
import com.android.sdk.mediaselector.bipicker.VideoCapturer
import com.android.sdk.mediaselector.bipicker.VideoPicker
import com.android.sdk.mediaselector.mediastore.MediaStoreSelector
import com.android.sdk.mediaselector.mediastore.SelectImageAction
import com.android.sdk.mediaselector.mediastore.SelectVideoAction
import com.android.sdk.mediaselector.processor.Processor
import com.android.sdk.mediaselector.processor.ProcessorManager

internal class AllInOneImplementation : BuiltInPicker, MediaStoreSelector, ComponentStateHandler {

    private var currentAction: Action? = null

    private val processorManager: ProcessorManager

    private val actFragWrapper: ActFragWrapper

    private val postProcessors = mutableListOf<Processor>()

    constructor(activity: AppCompatActivity, resultListener: ResultListener) {
        actFragWrapper = ActFragWrapper.create(activity)
        processorManager = ProcessorManager(actFragWrapper, activity, resultListener)
    }

    constructor(fragment: Fragment, resultListener: ResultListener) {
        actFragWrapper = ActFragWrapper.create(fragment)
        processorManager = ProcessorManager(actFragWrapper, fragment, resultListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        currentAction?.let {
            outState.putParcelable(CURRENT_ACTION_KEY, it)
            outState.putSerializable(CURRENT_ACTION_KEY, it.javaClass)
        }
        processorManager.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(outState: Bundle?) {
        if (outState != null) {
            val clazz = BundleCompat.getSerializable(outState, CURRENT_ACTION_CLASS_KEY, Class::class.java)
            clazz?.let {
                currentAction = (BundleCompat.getParcelable(outState, CURRENT_ACTION_KEY, it) as? Action)?.apply {
                    processorManager.install(assembleAllProcessors(this))
                }
            }
        }
        processorManager.onRestoreInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        processorManager.onActivityResult(requestCode, resultCode, data)
    }

    fun start(action: Action) {
        currentAction = action
        processorManager.install(assembleAllProcessors(action))
        processorManager.start()
    }

    private fun assembleAllProcessors(action: Action) = action.assembleProcessors(actFragWrapper) + postProcessors

    override fun captureImage(): ImageCapturer {
        return ImageCapturer().also {
            it.builtInSelector = this
        }
    }

    override fun captureVideo(): VideoCapturer {
        return VideoCapturer().also {
            it.builtInSelector = this
        }
    }

    override fun getContent(): GetContent {
        return GetContent().also {
            it.builtInSelector = this
        }
    }

    override fun getImageContent(): GetImageContent {
        return GetImageContent().also {
            it.builtInSelector = this
        }
    }

    override fun getVideoContent(): GetVideoContent {
        return GetVideoContent().also {
            it.builtInSelector = this
        }
    }

    override fun pickImage(): ImagePicker {
        return ImagePicker().also {
            it.builtInSelector = this
        }
    }

    override fun pickVideo(): VideoPicker {
        return VideoPicker().also {
            it.builtInSelector = this
        }
    }

    override fun pickImageAndVideo(): ImageAndVideoPicker {
        return ImageAndVideoPicker().also {
            it.builtInSelector = this
        }
    }

    override fun pickFile(): FilePicker {
        return FilePicker().also {
            it.builtInSelector = this
        }
    }

    override fun withPostProcessor(vararg processors: Processor) {
        postProcessors.addAll(processors)
    }


    override fun selectImage(): SelectImageAction {
        return SelectImageAction().also {
            it.builtInSelector = this
        }
    }

    override fun selectVideo(): SelectVideoAction {
        return SelectVideoAction().also {
            it.builtInSelector = this
        }
    }

    companion object {
        private const val CURRENT_ACTION_KEY = "current_action_key"
        private const val CURRENT_ACTION_CLASS_KEY = "current_action_class_key"
    }

}