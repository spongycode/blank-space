package com.spongycode.blankspace.ui.photoviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.spongycode.blankspace.R

class PhotoViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_photo_viewer)
        this.supportActionBar?.hide()

        val imageUrl = intent.getStringExtra("IMAGE_URL")
        Glide.with(applicationContext).load(imageUrl).into(findViewById(R.id.fullscreen_content))
    }
}