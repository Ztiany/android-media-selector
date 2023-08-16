package me.ztiany.media.selector.example

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import timber.log.Timber
import java.io.File
import java.io.InputStream

const val KEY = "Results"

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val list = intent.getParcelableArrayListExtra(KEY) ?: emptyList<Uri>()

        Timber.d("list: $list")

        val rv = findViewById<RecyclerView>(R.id.rv_result)
        PagerSnapHelper().attachToRecyclerView(rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val imageView = AppCompatImageView(this@ResultActivity)
                imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setOnClickListener {
                    (it.tag as? Uri)?.let(::showMediaInfoChecked)
                }
                return object : RecyclerView.ViewHolder(imageView) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                holder.itemView.tag = list[position]
                Glide.with(holder.itemView).load(list[position]).into(holder.itemView as ImageView)
            }

            override fun getItemCount(): Int {
                return list.size
            }
        }
    }

    private fun showMediaInfoChecked(uri: Uri) {
        if (uri.toString().startsWith("content://")) {
            showMediaInfo(uri, contentResolver.openInputStream(uri))
        } else {
            uri.path?.let {
                val file = File(it)
                Timber.d("showMediaInfoChecked file exist = ${file.exists()}")
                showMediaInfo(uri, file.inputStream())
            }
        }
    }

    private fun showMediaInfo(uri: Uri, stream: InputStream?) {
        Timber.d("showMediaInfo() called with: uri = $uri, stream = $stream")
        stream?.use {
            ExifInterface(stream).run {
                val latLong = latLong ?: doubleArrayOf(0.0, 0.0)
                Timber.e("%s latLong = %s", uri.toString(), latLong.contentToString())
            }
        }
    }

}