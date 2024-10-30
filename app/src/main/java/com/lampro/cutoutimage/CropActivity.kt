package com.lampro.cutoutimage

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CropActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crop)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cropView = findViewById<CropView>(R.id.cropView)
        val tvNext = findViewById<TextView>(R.id.tvNext)
        tvNext.setOnClickListener{
            val croppedImage : ByteArray = cropView.getCroppedImage()
            val intent = Intent(this, OutputActivity::class.java)
            intent.putExtra("croppedImage", croppedImage)
            startActivity(intent)
        }
    }
}