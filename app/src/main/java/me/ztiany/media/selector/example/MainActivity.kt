package me.ztiany.media.selector.example

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.sdk.mediaselector.common.ResultListener
import com.android.sdk.mediaselector.custom.newMediaSelector
import com.android.sdk.mediaselector.system.newSystemMediaSelector
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

    private var takingFile = false
    private lateinit var fileTextView: TextView

    private fun showResult(results: List<Uri>) {
        if (takingFile) {
            takingFile = false
            showFiles(results)
            return
        }
        showMedias(results)
    }

    private fun showMedias(results: List<Uri>) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putParcelableArrayListExtra(KEY, ArrayList(results))
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun showFiles(results: List<Uri>) {
        val files = results.joinToString(separator = "\n\n   ") {
            it.toString()
        }
        fileTextView.text = "Selected Files:\n\n   $files"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fileTextView = findViewById(R.id.selected_files)
        Timber.plant(DebugTree())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Support
    ///////////////////////////////////////////////////////////////////////////

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
    // Photos by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun captureOnePhoto(view: View) {
        askCameraOnly(false) {
            systemMediaSelector.takePhotoByCamera().start()
        }
    }

    fun captureOnePhotoAndCrop(view: View) {
        askCameraOnly(false) {
            systemMediaSelector.takePhotoByCamera().crop().start()
        }
    }

    fun selectOnePhoto(view: View) {
        askMediaPermissionWhenUsingSAF {
            systemMediaSelector.takePhotoFromSystem().start()
        }
    }

    fun selectOnePhotoAndCrop(view: View) {
        askMediaPermissionWhenUsingSAF {
            systemMediaSelector.takePhotoFromSystem().crop().start()
        }
    }

    fun selectPhotos(view: View) {
        askMediaPermissionWhenUsingSAF {
            systemMediaSelector.takePhotoFromSystem().multiple(true).start()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // MediaStore
    ///////////////////////////////////////////////////////////////////////////
    fun selectOnePhotoByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.takePicture().start()
            }
        }
    }

    fun selectOnePhotoWithCameraByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.takePicture().needMediaLocation().enableCamera().start()
            }
        }
    }

    fun selectOnePhotoWithCameraAndCropByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.takePicture().needMediaLocation().enableCamera().crop().needGif().start()
            }
        }
    }

    fun selectMultiPhotoByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.takePicture().needMediaLocation().count(9).needGif().start()
            }
        }
    }

    fun selectOneVideoByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.takeVideo().needMediaLocation().start()
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Files by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun selectFile(view: View) {
        askMediaPermissionWhenUsingSAF {
            takingFile = true
            systemMediaSelector.takeFileFromSystem().start()
        }
    }

    fun selectFiles(view: View) {
        askMediaPermissionWhenUsingSAF {
            takingFile = true
            systemMediaSelector.takeFileFromSystem().multiple(true).start()
        }
    }

}