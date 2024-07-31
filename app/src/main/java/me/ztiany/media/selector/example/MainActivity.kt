package me.ztiany.media.selector.example

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.android.base.delegate.simpl.DelegateActivity
import com.android.sdk.mediaselector.newMediaSelector
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainActivity : DelegateActivity() {

    private val mediaSelector = newMediaSelector { result ->
        result.forEach {
            Timber.e("item :$it")
        }
        showResult(result.map { it.uri })
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
        mediaSelector.selectImage().count(9).includeGif().start()
    }

    fun selectOneVideoByMediaStore(view: View) {
        mediaSelector.selectVideo().start()
    }

}