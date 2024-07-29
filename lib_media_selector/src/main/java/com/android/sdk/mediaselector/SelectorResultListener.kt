package com.android.sdk.mediaselector


interface SelectorResultListener {

    fun onResult(result: List<Item>)

    /** Check out logs for detailed information. */
    fun onFailed() {}

    fun onCanceled() {}

}