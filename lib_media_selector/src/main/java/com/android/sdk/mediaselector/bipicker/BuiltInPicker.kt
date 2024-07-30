package com.android.sdk.mediaselector.bipicker

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.base.delegate.activity.ActivityDelegateOwner
import com.android.base.delegate.fragment.FragmentDelegateOwner
import com.android.sdk.mediaselector.AllInOneImplementation
import com.android.sdk.mediaselector.ComponentStateHandler
import com.android.sdk.mediaselector.ResultListener
import com.android.sdk.mediaselector.autoCallback
import com.android.sdk.mediaselector.processor.Processor

/**
 *  Get photos and files through the system camera, PhotoPicker or SAF.
 *
 * @author Ztiany
 */
interface BuiltInPicker : ComponentStateHandler {

    fun captureImage(): ImageCapturer

    fun captureVideo(): VideoCapturer

    fun getContent(): GetContent

    fun getImageContent(): GetImageContent

    fun getVideoContent(): GetVideoContent

    fun pickImage(): ImagePicker

    fun pickVideo(): VideoPicker

    fun pickImageAndVideo(): ImageAndVideoPicker

    fun pickFile(): FilePicker

    fun withPostProcessor(vararg processors: Processor)

}

/**
 * If your fragment has implemented [FragmentDelegateOwner], you don't need to call methods in [ComponentStateHandler].
 *
 *@author Ztiany.
 */
fun Fragment.newBuiltInPicker(resultListener: ResultListener): BuiltInPicker {
    return AllInOneImplementation(this, resultListener).also {
        autoCallback(this, it)
    }
}

/**
 * If your activity has implemented [ActivityDelegateOwner], you don't need to call methods in [ComponentStateHandler].
 *
 *@author Ztiany
 */
fun AppCompatActivity.newBuiltInPicker(resultListener: ResultListener): BuiltInPicker {
    return AllInOneImplementation(this, resultListener).also {
        autoCallback(this, it)
    }
}