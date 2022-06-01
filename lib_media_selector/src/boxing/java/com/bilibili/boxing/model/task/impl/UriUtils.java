package com.bilibili.boxing.model.task.impl;

import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

class UriUtils {

    static Uri getExternalImageUriVersionChecked() {
        Uri mediaCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            /*The content:// style URI for the "primary" external storage volume.*/
            mediaCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        return mediaCollection;
    }

    static Uri getExternalVideoUriVersionChecked() {
        Uri mediaCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaCollection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            /*The content:// style URI for the "primary" external storage volume.*/
            mediaCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        return mediaCollection;
    }

}
