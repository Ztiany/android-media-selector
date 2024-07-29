package me.ztiany.media.selector.example

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.sdk.mediaselector.Item
import com.android.sdk.mediaselector.ResultListener
import com.android.sdk.mediaselector.SelectorResultListener
import com.android.sdk.mediaselector.bipicker.newSystemMediaSelector
import com.android.sdk.mediaselector.bipicker.newSystemMediaSelector2
import com.android.sdk.mediaselector.mediastore.newMediaSelector
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : AppCompatActivity() {

    private val systemMediaSelector by lazy {
        newSystemMediaSelector(this, object : ResultListener {
            override fun onResult(result: List<Uri>) {
                Timber.e("results :$result")
                result.forEach {
                    Timber.e("result :$it")
                }
                showResult(result)
            }
        })
    }

    private val systemMediaSelector2 by lazy {
        newSystemMediaSelector2(this, object : SelectorResultListener {
            override fun onResult(result: List<Item>) {
                Timber.e("results :$result")
                result.forEach {
                    Timber.e("result :$it")
                }
                showResult(result.map { it.uri })
            }
        })
    }

    private val mediaSelector by lazy {
        newMediaSelector(this, object : ResultListener {
            override fun onResult(result: List<Uri>) {
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
        systemMediaSelector2.onRestoreInstanceState(savedInstanceState)
        mediaSelector.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        systemMediaSelector.onSaveInstanceState(outState)
        systemMediaSelector2.onSaveInstanceState(outState)
        mediaSelector.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        systemMediaSelector.onActivityResult(requestCode, resultCode, data)
        systemMediaSelector2.onActivityResult(requestCode, resultCode, data)
        mediaSelector.onActivityResult(requestCode, resultCode, data)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Photos by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun captureOnePhoto(view: View) {
        askCameraOnly(false) {
            systemMediaSelector2.capturePhoto().start()
        }
    }

    fun captureOnePhotoAndCrop(view: View) {
        askCameraOnly(false) {
            systemMediaSelector2.capturePhoto().crop().start()
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