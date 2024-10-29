package com.lampro.cutoutimage

import android.content.ContentValues.TAG
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class OutputActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_output)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bitmapUri = intent.getStringExtra("bitmapUri")?.let { Uri.parse(it) }
        val bitmap = bitmapUri?.let { uri ->
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }


        val imgOutput = findViewById<ImageView>(R.id.imgOutput)

        Log.e("TAG", "onCreate: " + bitmap )
//        imgOutput.setImageBitmap(bitmap)
        Glide.with(this).load(bitmap).into(imgOutput)

    }
}