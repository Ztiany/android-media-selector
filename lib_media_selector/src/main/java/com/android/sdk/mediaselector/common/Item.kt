package com.android.sdk.mediaselector.common

import android.net.Uri

/** TODO: return Item instead of uri.*/
data class Item(
    val uri: Uri,
    var rawUri: Uri
)