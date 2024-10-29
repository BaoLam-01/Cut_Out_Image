package com.lampro.cutoutimage

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lampro.cutoutimage.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cutOutView = findViewById<CutOutView>(R.id.cutOutView)
        val btnNext = findViewById<TextView>(R.id.tvNext)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.meo)
        cutOutView.setImageBitmap(bitmap)

        cutOutView.setOnCutOutListener { isReady ->
                if (isReady) {
                    btnNext.visibility = View.VISIBLE
                } else {
                    btnNext.visibility = View.INVISIBLE
                }
        }



        btnNext.setOnClickListener{

            val outBitmap = cutOutView.cropImageWithPath()

            val uri  = saveBitmapToFile(outBitmap,this)

            val intent = Intent(this, OutputActivity::class.java)

            intent.putExtra("bitmapUri", uri.toString())
            startActivity(intent)
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, context: Context): Uri {
        val file = File(context.cacheDir, "cutOutImage.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.toUri()
    }

}