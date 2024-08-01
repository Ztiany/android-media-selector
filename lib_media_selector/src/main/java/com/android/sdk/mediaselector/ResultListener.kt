package com.android.sdk.mediaselector

interface ResultListener {

    /** Check out logs for detailed information. */
    fun onFailed() {}

    fun onCanceled() {}

    fun onResult(result: List<MediaItem>) {
        if (result.size == 1) {
            onSingleResult(result.first())
        }
    }

    fun onSingleResult(result: MediaItem) {}

}