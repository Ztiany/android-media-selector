package me.ztiany.media.selector.example

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.android.base.delegate.simpl.DelegateActivity
import com.android.sdk.mediaselector.MediaItem
import com.android.sdk.mediaselector.SelectorConfigurer
import com.android.sdk.mediaselector.buildMediaSelector
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : DelegateActivity() {

    private val mediaSelector = buildMediaSelector {
        withProcessor(imageCompressor())
        onResults { handleSelectedItems(it) }
    }

    private fun handleSelectedItems(results: List<MediaItem>) {
        results.forEach {
            Timber.e("item :$it")
        }

        if (takingByMediaStore) {
            selectedItems = results
            takingByMediaStore = false
        }

        if (takingFile) {
            takingFile = false
            showFiles(results)
        } else {
            showMedias(results)
        }
    }


    private var takingFile = false

    private var takingByMediaStore = false

    private lateinit var fileTextView: TextView

    private var selectedItems = emptyList<MediaItem>()

    override fun initialize(savedInstanceState: Bundle?) {
        Timber.plant(DebugTree())

        SelectorConfigurer.setUp {
            setCropPrimaryColorAttr(androidx.appcompat.R.attr.colorAccent)
        }
    }

    override fun provideLayout() = R.layout.activity_main

    override fun setUpLayout(savedInstanceState: Bundle?) {
        fileTextView = findViewById(R.id.selected_files)
    }

    private fun showMedias(results: List<MediaItem>) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putParcelableArrayListExtra(KEY, ArrayList(results))
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun showFiles(results: List<MediaItem>) {
        val files = results.joinToString(separator = "\n\n   ") {
            it.toString()
        }
        fileTextView.text = "Selected Files:\n\n   $files"
    }

    ///////////////////////////////////////////////////////////////////////////
    // Photos by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun captureOnePhoto(view: View) {
        mediaSelector.captureImage().start()
    }

    fun captureOneVideo(view: View) {
        mediaSelector.captureVideo().start()
    }

    fun captureOnePhotoAndCrop(view: View) {
        mediaSelector.captureImage().crop().start()
    }

    fun selectOnePhoto(view: View) {
        mediaSelector.pickImage().start()
    }

    fun selectOnePhotoAndCrop(view: View) {
        mediaSelector.pickImage().crop().start()
    }

    fun selectPhotos(view: View) {
        mediaSelector.pickImage().count(4).start()
    }

    fun selectPhotosAndCrop(view: View) {
        mediaSelector.pickImage().count(4).crop().start()
    }

    fun selectVideos(view: View) {
        mediaSelector.pickVideo().count(4).start()
    }

    fun selectPhotoAndVideo(view: View) {
        mediaSelector.pickImageAndVideo().count(4).crop().start()
    }

    fun selectPhotosByGetContent(view: View) {
        mediaSelector.getImageContent().multiple().crop().start()
    }

    fun selectVideosByGetContent(view: View) {
        mediaSelector.getVideoContent().multiple().start()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Files by Intent or SAF
    ///////////////////////////////////////////////////////////////////////////
    fun selectFile(view: View) {
        takingFile = true
        mediaSelector.pickFile().start()
    }

    fun selectFiles(view: View) {
        takingFile = true
        mediaSelector.pickFile().multiple().start()
    }

    fun selectFilesByGetContent(view: View) {
        takingFile = true
        mediaSelector.getContent().multiple().start()
    }

    ///////////////////////////////////////////////////////////////////////////
    // MediaStore
    ///////////////////////////////////////////////////////////////////////////
    fun selectOnePhotoByMediaStore(view: View) {
        mediaSelector.selectImage().start()
    }

    fun selectOnePhotoWithCameraByMediaStore(view: View) {
        mediaSelector.selectImage().enableCamera().start()
    }

    fun selectOnePhotoWithCameraAndCropByMediaStore(view: View) {
        mediaSelector.selectImage().enableCamera().crop().includeGif().start()
    }

    fun selectMultiPhotoByMediaStore(view: View) {
        takingByMediaStore = true
        mediaSelector.selectImage().count(9).includeGif().selectedData(selectedItems.map { it.id }).start()
    }

    fun selectOneVideoByMediaStore(view: View) {
        mediaSelector.selectVideo().start()
    }

}