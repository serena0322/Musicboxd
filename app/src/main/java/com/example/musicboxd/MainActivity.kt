package com.example.musicboxd

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //sfumatura colore testo
        val textView = findViewById<TextView>(R.id.Title)
        textView.post {
            val textWidth = textView.paint.measureText(textView.text.toString())

            val shader = LinearGradient(
                0f, 0f, textWidth, 0f,  // <-- gradient orizzontale: da sinistra a destra
                intArrayOf(
                    Color.parseColor("#FF00FF"),  // Colore a sinistra
                    Color.parseColor("#00FFFF")   // Colore a destra
                ),
                null,
                Shader.TileMode.CLAMP
            )
            textView.paint.shader = shader
            textView.invalidate()
            //
        }


    }
}





