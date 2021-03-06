package com.android.sdk.mediaselector.custom

import android.net.Uri
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.sdk.mediaselector.common.MediaUtils
import com.android.sdk.mediaselector.common.ResultListener
import com.android.sdk.mediaselector.common.newUriList
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
                mediaSelectorCallback.onTakeFail()
            } else {
                toCrop(absolutePath)
            }
        }
    }

    override fun handleSingleCropResult(absolutePath: String) {
        mediaSelectorCallback.onTakeSuccess(newUriList(absolutePath))
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
            mediaSelectorCallback.onTakeFail()
        } else {
            mediaSelectorCallback.onTakeSuccess(result)
        }
    }

    private fun returnSingleDataChecked(uri: Uri) {
        val absolutePath = MediaUtils.getAbsolutePath(context, uri)
        if (TextUtils.isEmpty(absolutePath)) {
            mediaSelectorCallback.onTakeFail()
        } else {
            mediaSelectorCallback.onTakeSuccess(newUriList(absolutePath))
        }
    }

}