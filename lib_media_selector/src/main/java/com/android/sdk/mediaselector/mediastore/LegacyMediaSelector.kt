package com.android.sdk.mediaselector.mediastore

import android.net.Uri
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.sdk.mediaselector.utils.MediaUtils
import com.android.sdk.mediaselector.ResultListener
import com.android.sdk.mediaselector.utils.newUriList
import com.bilibili.boxing.model.entity.BaseMedia
import java.io.File

/**
 *@author Ztiany
 */
internal class LegacyMediaSelector : BaseMediaSelector {

    constructor(activity: AppCompatActivity, resultListener: ResultListener) : super(activity, resultListener)

    constructor(fragment: Fragment, resultListener: ResultListener) : super(fragment, resultListener)

    ///////////////////////////////////////////////////////////////////////////
    // handle single result
    ///////////////////////////////////////////////////////////////////////////
    override fun handleSingleResult(baseMedia: BaseMedia) {
        val cropOptions = currentInstruction.cropOptions
        if (cropOptions == null) {
            returnSingleDataChecked(baseMedia.uri)
        } else {
            val absolutePath = MediaUtils.getAbsolutePath(context, baseMedia.uri)
            if (absolutePath.isNullOrBlank()) {
                mediaSelectorCallback.onFailed()
            } else {
                toCrop(absolutePath)
            }
        }
    }

    override fun handleSingleCropResult(absolutePath: String) {
        mediaSelectorCallback.onResult(newUriList(absolutePath))
    }

    ///////////////////////////////////////////////////////////////////////////
    // handle multi results
    ///////////////////////////////////////////////////////////////////////////
    override fun handleMultiResult(medias: ArrayList<BaseMedia>) {
        val result = medias.mapNotNull {
            MediaUtils.getAbsolutePath(context, it.uri)?.run {
                Uri.fromFile(File(this))
            }
        }

        if (result.isEmpty()) {
            mediaSelectorCallback.onFailed()
        } else {
            mediaSelectorCallback.onResult(result)
        }
    }

    private fun returnSingleDataChecked(uri: Uri) {
        val absolutePath = MediaUtils.getAbsolutePath(context, uri)
        if (TextUtils.isEmpty(absolutePath)) {
            mediaSelectorCallback.onFailed()
        } else {
            mediaSelectorCallback.onResult(newUriList(absolutePath))
        }
    }

}