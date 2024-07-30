package com.android.sdk.mediaselector.mediastore

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
 * Get photos and videos by MediaStore API.
 *
 * If your activity has implemented [ActivityDelegateOwner], you don't need to call methods in [ComponentStateHandler].
 * And the same if your fragment has implemented [FragmentDelegateOwner].
 *
 *@author Ztiany
 */
interface MediaStoreSelector : ComponentStateHandler {

    fun selectImage(): SelectImageAction

    fun selectVideo(): SelectVideoAction

    fun withPostProcessor(vararg processors: Processor)

}

/**
 * If your activity has implemented [ActivityDelegateOwner], you don't need to call methods in [ComponentStateHandler].
 *
 *@author Ztiany
 */
fun AppCompatActivity.newMediaStoreSelector(resultListener: ResultListener): MediaStoreSelector {
    return AllInOneImplementation(this, resultListener).also {
        autoCallback(this, it)
    }
}

/**
 * If your fragment has implemented [FragmentDelegateOwner], you don't need to call methods in [ComponentStateHandler].
 *
 *@author Ztiany
 */
fun Fragment.newMediaStoreSelector(resultListener: ResultListener): MediaStoreSelector {
    return AllInOneImplementation(this, resultListener).also {
        autoCallback(this, it)
    }
}