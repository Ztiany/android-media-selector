package com.android.sdk.mediaselector.mediastore

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.sdk.mediaselector.utils.MediaUtils
import com.android.sdk.mediaselector.ResultListener
import com.android.sdk.mediaselector.utils.copySingleToInternal
import com.android.sdk.mediaselector.utils.newUriList
import com.bilibili.boxing.model.entity.BaseMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 *@author Ztiany
 */
@RequiresApi(Build.VERSION_CODES.Q)
internal class AndroidQMediaSelector : BaseMediaSelector {

    constructor(activity: AppCompatActivity, resultListener: ResultListener) : super(activity, resultListener)

    constructor(fragment: Fragment, resultListener: ResultListener) : super(fragment, resultListener)

    ///////////////////////////////////////////////////////////////////////////
    // handle single result
    ///////////////////////////////////////////////////////////////////////////
    override fun handleSingleResult(baseMedia: BaseMedia) {
        Timber.d("handleMultiResult() called with: medias = $baseMedia")

        //replace uri if need the access of media location.
        if (currentInstruction.isNeedAccessMediaLocation) {
            baseMedia.uri = MediaStore.setRequireOriginal(baseMedia.uri)
        }

        if (currentInstruction.isCopyToInternal || currentInstruction.isNeedCrop) {

            val absolutePath = MediaUtils.getAbsolutePath(context, baseMedia.uri) ?: ""
            val alreadyInternal = absolutePath.contains("Android/data/${context.packageName}")

            Timber.d("handleSingleResult alreadyInternal = $alreadyInternal")

            if (alreadyInternal) {
                when {
                    currentInstruction.isNeedCrop -> toCrop(absolutePath)
                    else -> mediaSelectorCallback.onResult(newUriList(absolutePath))
                }
            } else {
                copyAndHandleSingleResult(baseMedia)
            }

            return
        }

        mediaSelectorCallback.onResult(listOf(baseMedia.uri))
    }

    private fun copyAndHandleSingleResult(baseMedia: BaseMedia) {
        lifecycleOwner.lifecycleScope.launch {

            val copied = withContext(Dispatchers.IO) {
                copySingleToInternal(context, baseMedia.uri)
            }

            when {
                copied == null -> mediaSelectorCallback.onFailed()
                currentInstruction.isNeedCrop -> toCrop(copied)
                else -> mediaSelectorCallback.onResult(newUriList(copied))
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
        Timber.d("handleMultiResult() called with: medias = $medias")

        //replace uri if need the access of media location.
        if (currentInstruction.isNeedAccessMediaLocation) {
            medias.forEach {
                it.uri = MediaStore.setRequireOriginal(it.uri)
            }
        }

        if (currentInstruction.isCopyToInternal) {
            copyToInternalAndReturn(medias)
        } else {
            medias.map {
                it.uri
            }.let(mediaSelectorCallback::onResult)
        }
    }

    private fun copyToInternalAndReturn(medias: ArrayList<BaseMedia>) {
        lifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                medias.mapNotNull {
                    copySingleToInternal(context, it.uri)
                }
            }.map {
                Uri.fromFile(File(it))
            }.let {
                if (it.isEmpty()) {
                    mediaSelectorCallback.onFailed()
                } else {
                    mediaSelectorCallback.onResult(it)
                }
            }
        }
    }

}