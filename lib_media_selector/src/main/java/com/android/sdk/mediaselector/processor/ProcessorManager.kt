package com.android.sdk.mediaselector.processor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.android.sdk.mediaselector.ActFragWrapper
import com.android.sdk.mediaselector.ComponentStateHandler
import com.android.sdk.mediaselector.Item
import com.android.sdk.mediaselector.ResultListener
import timber.log.Timber

internal class ProcessorManager(
    private val actFragWrapper: ActFragWrapper,
    private val lifecycleOwner: LifecycleOwner,
    private val resultListener: ResultListener,
) : ComponentStateHandler {

    private val processors = mutableListOf<Processor>()

    private var processorProgress = 0

    private val processorChain = object : ProcessorChain {
        override fun onFailed() {
            resultListener.onFailed()
        }

        override fun onCanceled() {
            resultListener.onCanceled()
        }

        override fun onResult(uri: List<Uri>) {
            continueProcedure(uri)
        }
    }

    fun install(assembledProcessors: List<Processor>) {
        if (assembledProcessors.isEmpty()) {
            throw IllegalStateException("assembledProcessors is empty.")
        }
        processors.clear()
        processors.addAll(assembledProcessors)
        processors.forEach { it.onAttachToChain(processorChain) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        processors.forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        processors.forEach { it.onSaveInstanceState(outState) }
    }

    override fun onRestoreInstanceState(outState: Bundle?) {
        processors.forEach { it.onRestoreInstanceState(outState) }
    }

    fun start() {
        processorProgress = 0
        continueProcedure(emptyList())
    }

    private fun continueProcedure(params: List<Uri>) {
        val processor = processors.getOrNull(processorProgress)
        Timber.d("continueProcedure: $processor")
        if (processor == null) {
            onAllProcessorCompleted(params)
            return
        }
        processor.start(params)
        processorProgress++
    }

    private fun onAllProcessorCompleted(result: List<Uri>) {
        resultListener.onResult(result.map {
            Item(
                id = "",
                uri = it,
                middle = emptyMap(),
            )
        })
    }

}