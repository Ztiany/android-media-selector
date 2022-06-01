package com.android.sdk.mediaselector.custom

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.base.delegate.activity.ActivityDelegateOwner
import com.android.base.delegate.fragment.FragmentDelegateOwner
import com.android.sdk.mediaselector.common.ActivityStateHandler
import com.android.sdk.mediaselector.common.ResultListener
import com.android.sdk.mediaselector.common.autoCallback
import timber.log.Timber

/**
 * If your activity has implemented [ActivityDelegateOwner], you don't call methods in [ActivityStateHandler].
 * And the same if your fragment has implemented [FragmentDelegateOwner].
 *
 *@author Ztiany
 */
interface MediaSelector : ActivityStateHandler {

    fun takePicture(): Instruction

    fun takeVideo(): Instruction

}

/**
 * If your activity has implemented [ActivityDelegateOwner], you don't call methods in [ActivityStateHandler].
 *
 *@author Ztiany
 */
fun newMediaSelector(activity: AppCompatActivity, resultListener: ResultListener): MediaSelector {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Timber.d("newSystemMediaSelector LegacySystemMediaSelector")
        LegacyMediaSelector(activity, resultListener)
    } else {
        Timber.d("newSystemMediaSelector AndroidPSystemMediaSelector")
        AndroidQMediaSelector(activity, resultListener)
    }.also {
        autoCallback(activity, it)
    }
}

/**
 * If your fragment has implemented [FragmentDelegateOwner], you don't call methods in [ActivityStateHandler].
 *
 *@author Ztiany
 */
fun newMediaSelector(fragment: Fragment, resultListener: ResultListener): MediaSelector {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Timber.d("newSystemMediaSelector LegacySystemMediaSelector")
        LegacyMediaSelector(fragment, resultListener)
    } else {
        Timber.d("newSystemMediaSelectorAndroidPSystemMediaSelector")
        AndroidQMediaSelector(fragment, resultListener)
    }.also {
        autoCallback(fragment, it)
    }
}
