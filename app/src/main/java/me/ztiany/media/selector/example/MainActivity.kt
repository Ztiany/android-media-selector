package me.ztiany.media.selector.example

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.android.base.delegate.simpl.DelegateActivity
import com.android.sdk.mediaselector.Item
import com.android.sdk.mediaselector.ResultListener
import com.android.sdk.mediaselector.bipicker.newBuiltInPicker
import com.android.sdk.mediaselector.mediastore.newMediaStoreSelector
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : DelegateActivity() {

    private val builtInPicker by lazy {
        newBuiltInPicker(object : ResultListener {
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
        newMediaStoreSelector(object : ResultListener {
            override fun onResult(result: List<Item>) {
                Timber.e("results :$result")
                result.forEach {
                    Timber.e("result :$it")
                }
                showResult(result.map { it.uri })
            }
        })
    }

    private var takingFile = false

    private lateinit var fileTextView: TextView

    override fun initialize(savedInstanceState: Bundle?) {
        Timber.plant(DebugTree())
    }

    override fun provideLayout() = R.layout.activity_main

    override fun setUpLayout(savedInstanceState: Bundle?) {
        fileTextView = findViewById(R.id.selected_files)
    }

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

    ///////////////////////////////////////////////////////////////////////////
    // Photos by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun captureOnePhoto(view: View) {
        askCameraOnly(false) {
            builtInPicker.captureImage().start()
        }
    }

    fun captureOneVideo(view: View) {
        askCameraOnly(false) {
            builtInPicker.captureVideo().start()
        }
    }

    fun captureOnePhotoAndCrop(view: View) {
        askCameraOnly(false) {
            builtInPicker.captureImage().crop().start()
        }
    }

    fun selectOnePhoto(view: View) {
        askMediaPermissionWhenUsingSAF {
            builtInPicker.pickImage().start()
        }
    }

    fun selectOnePhotoAndCrop(view: View) {
        askMediaPermissionWhenUsingSAF {
            builtInPicker.pickImage().crop().start()
        }
    }

    fun selectPhotos(view: View) {
        askMediaPermissionWhenUsingSAF {
            builtInPicker.pickImage().count(4).start()
        }
    }

    fun selectPhotosAndCrop(view: View) {
        askMediaPermissionWhenUsingSAF {
            builtInPicker.pickImage().count(4).crop().start()
        }
    }

    fun selectVideos(view: View) {
        askMediaPermissionWhenUsingSAF {
            builtInPicker.pickVideo().count(4).start()
        }
    }

    fun selectPhotoAndVideo(view: View) {
        askMediaPermissionWhenUsingSAF {
            builtInPicker.pickImageAndVideo().count(4).crop().start()
        }
    }

    fun selectPhotosByGetContent(view: View) {
        askMediaPermissionWhenUsingSAF {
            builtInPicker.getImageContent().multiple().crop().start()
        }
    }

    fun selectVideosByGetContent(view: View) {
        askMediaPermissionWhenUsingSAF {
            builtInPicker.getVideoContent().multiple().start()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Files by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun selectFile(view: View) {
        askMediaPermissionWhenUsingSAF {
            takingFile = true
            builtInPicker.pickFile().start()
        }
    }

    fun selectFiles(view: View) {
        askMediaPermissionWhenUsingSAF {
            takingFile = true
            builtInPicker.pickFile().multiple().start()
        }
    }

    fun selectFilesByGetContent(view: View) {
        askMediaPermissionWhenUsingSAF {
            takingFile = true
            builtInPicker.getContent().multiple().start()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // MediaStore
    ///////////////////////////////////////////////////////////////////////////
    fun selectOnePhotoByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.selectImage().start()
            }
        }
    }

    fun selectOnePhotoWithCameraByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.selectImage().acquireLocation().enableCamera().start()
            }
        }
    }

    fun selectOnePhotoWithCameraAndCropByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.selectImage().acquireLocation().enableCamera().crop().includeGif().start()
            }
        }
    }

    fun selectMultiPhotoByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.selectImage().acquireLocation().count(9).includeGif().start()
            }
        }
    }

    fun selectOneVideoByMediaStore(view: View) {
        askMediaPermissionWhenUsingMediaAPI {
            if (it != PermissionState.None) {
                mediaSelector.selectVideo().acquireLocation().start()
            }
        }
    }

}