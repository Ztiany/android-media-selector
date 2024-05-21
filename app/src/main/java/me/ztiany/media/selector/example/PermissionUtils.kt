package me.ztiany.media.selector.example

import android.Manifest.permission.ACCESS_MEDIA_LOCATION
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX
import timber.log.Timber

fun AppCompatActivity.checkLegacyExternalStorage(): Boolean {
    val packageManager = packageManager
    val packageName = packageName

    val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
        // For Android Q (API level 29), requestLegacyExternalStorage is always false unless explicitly set in the manifest.
        // For Android Q After (API level 30 or higher), requestLegacyExternalStorage is always useless.
        applicationInfo.metaData?.getBoolean("android:requestLegacyExternalStorage", false) ?: false
    } else {
        // For versions before Android Q, requestLegacyExternalStorage is not applicable
        false
    }
}

fun AppCompatActivity.askCameraOnly(needLocation: Boolean, onGranted: () -> Unit) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q/*Android 10, API 29*/) {
        Timber.d("Build.VERSION_CODES.Q: askCameraOnly, needLocation = $needLocation")
        if (needLocation) {
            arrayOf(CAMERA, ACCESS_MEDIA_LOCATION)
        } else {
            arrayOf(CAMERA)
        }
    } else /* Old */ {
        Timber.d("Build.VERSION_CODES.OLD: askCameraOnly")
        arrayOf(CAMERA)
    }

    PermissionX.init(this)
        .permissions(*permissions)
        .request { allGranted, _, deniedList ->
            if (allGranted) {
                Timber.d("askCameraOnly, All permissions are granted")
                onGranted()
            } else {
                Timber.d("askCameraOnly, These permissions are denied: $deniedList")
            }
        }
}

fun AppCompatActivity.askMediaPermissionWhenUsingSAF(onGranted: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Timber.d("Build.VERSION_CODES.Q: askMediaPermissionWhenUsingSAF")
        onGranted()
        return
    }

    Timber.d("Build.VERSION_CODES.OLD: askMediaPermissionWhenUsingSAF")

    PermissionX.init(this)
        .permissions(READ_EXTERNAL_STORAGE)
        .request { allGranted, _, deniedList ->
            if (allGranted) {
                Timber.d("askMediaPermissionWhenUsingSAF, All permissions are granted")
                onGranted()
            } else {
                Timber.d("askMediaPermissionWhenUsingSAF, These permissions are denied: $deniedList")
                Timber.d("")
            }
        }
}

enum class PermissionState {
    Full,
    Visual,
    None
}

fun AppCompatActivity.askMediaPermissionWhenUsingMediaAPI(onGranted: (state: PermissionState) -> Unit) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE /*Android 14, API 34*/) {

        Timber.d("Build.VERSION_CODES.UPSIDE_DOWN_CAKE")
        arrayOf(CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_VISUAL_USER_SELECTED, ACCESS_MEDIA_LOCATION)

    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU /*Android 13, API 33*/) {

        Timber.d("Build.VERSION_CODES.TIRAMISU")
        arrayOf(CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, ACCESS_MEDIA_LOCATION)

    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q/*Android 10, API 29*/) {

        Timber.d("Build.VERSION_CODES.Q")
        arrayOf(CAMERA, READ_EXTERNAL_STORAGE, ACCESS_MEDIA_LOCATION)

    } else /* Old */ {
        Timber.d("Build.VERSION_CODES.OLD")
        arrayOf(CAMERA, READ_EXTERNAL_STORAGE)
    }

    PermissionX.init(this)
        .permissions(*permissions)
        .request { allGranted, grantedList, deniedList ->
            if (allGranted) {
                Timber.d("askMediaPermissionWhenUsingMediaAPI, All permissions are granted")
                onGranted(PermissionState.Full)
            } else {
                Timber.d("These permissions are denied: $deniedList")
                if (grantedList.any { it != "android.permission.ACCESS_MEDIA_LOCATION" && it != CAMERA }) {
                    onGranted(PermissionState.Visual)
                    Timber.d("askMediaPermissionWhenUsingMediaAPI, PermissionState.Visual")
                } else {
                    Timber.d("askMediaPermissionWhenUsingMediaAPI, PermissionState.None")
                    onGranted(PermissionState.None)
                }
            }
        }
}