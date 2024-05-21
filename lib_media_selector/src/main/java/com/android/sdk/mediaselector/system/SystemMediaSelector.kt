@file:JvmName("SystemMediaSelectorCreator")

package com.android.sdk.mediaselector.system

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.base.delegate.activity.ActivityDelegateOwner
import com.android.base.delegate.fragment.FragmentDelegateOwner
import com.android.sdk.mediaselector.common.ActivityStateHandler
import com.android.sdk.mediaselector.common.MediaSelectorConfiguration
import com.android.sdk.mediaselector.common.ResultListener
import com.android.sdk.mediaselector.common.autoCallback
import timber.log.Timber

/**
 *  Get photos and files through the system camera or SAF.
 *
 * @author Ztiany
 */
interface SystemMediaSelector : ActivityStateHandler {

    fun takePhotoByCamera(): Instruction

    fun takePhotoFromSystem(): Instruction

    fun takeFileFromSystem(): Instruction

}

/**
 * If your activity has implemented [ActivityDelegateOwner], you don't call methods in [ActivityStateHandler].
 *
 *@author Ztiany
 */
fun newSystemMediaSelector(activity: AppCompatActivity, resultListener: ResultListener): SystemMediaSelector {
    return if (Build.VERSION.SDK_INT < 29 || MediaSelectorConfiguration.isForceUseLegacyApi()) {
        Timber.d("newSystemMediaSelector LegacySystemMediaSelector")
        LegacySystemMediaSelector(activity, resultListener)
    } else {
        Timber.d("newSystemMediaSelector AndroidQSystemMediaSelector")
        AndroidQSystemMediaSelector(activity, resultListener)
    }.also {
        autoCallback(activity, it)
    }
}

/**
 * If your fragment has implemented [FragmentDelegateOwner], you don't call methods in [ActivityStateHandler].
 *
 *@author Ztiany.
 */
fun newSystemMediaSelector(fragment: Fragment, resultListener: ResultListener): SystemMediaSelector {
    return if (Build.VERSION.SDK_INT < 29 || MediaSelectorConfiguration.isForceUseLegacyApi()) {
        Timber.d("newSystemMediaSelector LegacySystemMediaSelector")
        LegacySystemMediaSelector(fragment, resultListener)
    } else {
        Timber.d("newSystemMediaSelector AndroidQSystemMediaSelector")
        AndroidQSystemMediaSelector(fragment, resultListener)
    }.also {
        autoCallback(fragment, it)
    }
}