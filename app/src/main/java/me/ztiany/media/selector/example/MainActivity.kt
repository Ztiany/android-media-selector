package me.ztiany.media.selector.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.sdk.mediaselector.common.ResultListener
import com.android.sdk.mediaselector.custom.newMediaSelector
import com.android.sdk.mediaselector.system.newSystemMediaSelector
import com.permissionx.guolindev.PermissionX
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : AppCompatActivity() {

    private val systemMediaSelector by lazy {
        newSystemMediaSelector(this, object : ResultListener {
            override fun onTakeSuccess(result: List<Uri>) {
                Timber.e("results :$result")
                result.forEach {
                    Timber.e("result :$it")
                }
                showResult(result)
            }
        })
    }

    private val mediaSelector by lazy {
        newMediaSelector(this, object : ResultListener {
            override fun onTakeSuccess(result: List<Uri>) {
                Timber.e("results :$result")
                result.forEach {
                    Timber.e("result :$it")
                }
                showResult(result)
            }
        })
    }


    private fun showResult(results: List<Uri>) {
        if (takingFile) {
            takingFile = false
            return
        }
        val intent = Intent(this, ResultActivity::class.java)
        intent.putParcelableArrayListExtra(KEY, ArrayList(results))
        startActivity(intent)
    }

    private var takingFile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(DebugTree())
        askNecessaryPermissions()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Support
    ///////////////////////////////////////////////////////////////////////////
    private fun askNecessaryPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Timber.d("Build.VERSION_CODES.TIRAMISU")
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.ACCESS_MEDIA_LOCATION)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Timber.d("Build.VERSION_CODES.Q")
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION)
        } else {
            Timber.d("Build.VERSION_CODES.OLD")
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        PermissionX.init(this)
            .permissions(*permissions)
            .request { allGranted, _, deniedList ->
                if (allGranted) {
                    Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                    supportFinishAfterTransition()
                }
            }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        systemMediaSelector.onRestoreInstanceState(savedInstanceState)
        mediaSelector.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        systemMediaSelector.onSaveInstanceState(outState)
        mediaSelector.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        systemMediaSelector.onActivityResult(requestCode, resultCode, data)
        mediaSelector.onActivityResult(requestCode, resultCode, data)
    }

    ///////////////////////////////////////////////////////////////////////////
    // MediaStore
    ///////////////////////////////////////////////////////////////////////////
    fun selectOnePhotoByMediaStore(view: View) {
        mediaSelector.takePicture().start()
    }

    fun selectOnePhotoWithCameraByMediaStore(view: View) {
        mediaSelector.takePicture().needMediaLocation().enableCamera().start()
    }

    fun selectOnePhotoWithCameraAndCropByMediaStore(view: View) {
        mediaSelector.takePicture().needMediaLocation().enableCamera().crop().needGif().start()
    }

    fun selectMultiPhotoByMediaStore(view: View) {
        mediaSelector.takePicture().needMediaLocation().count(9).needGif().start()
    }

    fun selectOneVideoByMediaStore(view: View) {
        mediaSelector.takeVideo().needMediaLocation().start()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Photos by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun captureOnePhoto(view: View) {
        systemMediaSelector.takePhotoByCamera().start()
    }

    fun captureOnePhotoAndCrop(view: View) {
        systemMediaSelector.takePhotoByCamera().crop().start()
    }

    fun selectOnePhoto(view: View) {
        systemMediaSelector.takePhotoFromSystem().start()
    }

    fun selectOnePhotoAndCrop(view: View) {
        systemMediaSelector.takePhotoFromSystem().crop().start()
    }

    fun selectPhotos(view: View) {
        systemMediaSelector.takePhotoFromSystem().multiple(true).start()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Files by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun selectFile(view: View) {
        takingFile = true
        systemMediaSelector.takeFileFromSystem().start()
    }

    fun selectFiles(view: View) {
        takingFile = true
        systemMediaSelector.takeFileFromSystem().multiple(true).start()
    }

}