package com.android.sdk.mediaselector.bipicker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.sdk.mediaselector.ActivityStateHandler
import com.android.sdk.mediaselector.SelectorResultListener
import com.android.sdk.mediaselector.processor.ProcessorManager
import com.android.sdk.mediaselector.utils.ActFragWrapper

internal class BuiltInSelectorImpl : BuiltInSelector, ActivityStateHandler {

    private lateinit var currentAction: Action

    private val processorManager: ProcessorManager

    private val actFragWrapper: ActFragWrapper

    constructor(activity: AppCompatActivity, resultListener: SelectorResultListener) {
        actFragWrapper = ActFragWrapper.create(activity)
        processorManager = ProcessorManager(actFragWrapper, activity, resultListener)
    }

    constructor(fragment: Fragment, resultListener: SelectorResultListener) {
        actFragWrapper = ActFragWrapper.create(fragment)
        processorManager = ProcessorManager(actFragWrapper, fragment, resultListener)
    }

    fun start(action: Action) {
        currentAction = action
        processorManager.install(currentAction.assembleProcessors(actFragWrapper))
        processorManager.start()
    }

    override fun capturePhoto(): CapturePhoto {
        return CapturePhoto().also {
            it.builtInSelector = this
        }
    }

    override fun pickPhoto(): PickPhoto {
        return PickPhoto().also {
            it.builtInSelector = this
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        processorManager.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(outState: Bundle?) {
        processorManager.onRestoreInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        processorManager.onActivityResult(requestCode, resultCode, data)
    }

}