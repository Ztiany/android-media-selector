package com.android.sdk.mediaselector

import android.net.Uri

data class Item(
    val rawUri: Uri,
    val middleUri: Map<String, Uri>,
    val uri: Uri,
)