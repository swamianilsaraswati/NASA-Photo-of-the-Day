package com.example.nasaphotooftheday

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_full_screen_photo.*


class FullScreenPhoto : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_photo)

        //hide action bar
        supportActionBar?.hide()

        //get values passed from called activity
        val extras = intent.extras
        val hdurl = extras!!.getString("hdurl")

        //load image
        Picasso.get().load(hdurl).into(imageView2);

        //pinch to zoom
        imageView2.setOnTouchListener(ImageMatrixTouchHandler(applicationContext))
    }
}