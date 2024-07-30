package com.android.sdk.mediaselector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

class ActFragWrapper private constructor(
    val fragment: Fragment?,
    val activity: Activity?,
) {

    val context: Context
        get() = activity ?: (fragment?.requireContext() ?: throw IllegalStateException("never happen."))

    fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle? = null) {
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode, options)
        } else fragment?.startActivityForResult(intent, requestCode, options)
    }

    companion object {

        fun create(activity: Activity): ActFragWrapper {
            return ActFragWrapper(activity = activity, fragment = null)
        }

        fun create(fragment: Fragment?): ActFragWrapper {
            return ActFragWrapper(fragment = fragment, activity = null)
        }

    }

}