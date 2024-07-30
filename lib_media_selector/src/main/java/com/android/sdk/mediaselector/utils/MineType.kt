package com.android.sdk.mediaselector.utils

enum class MineType(val value: String, val formats: List<String>) {
    IMAGE("image/*", listOf("jpg", "jpeg", "png", "gif", "bmp")),
    VIDEO("video/*", listOf("mp4", "3gp", "mkv", "avi", "flv")),
    ALL("*/*", emptyList());
}