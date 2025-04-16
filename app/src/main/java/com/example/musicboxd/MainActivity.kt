package com.example.musicboxd

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //sfumatura colore testo
        val textView = findViewById<TextView>(R.id.Title)
        textView.post {
            val textWidth = textView.paint.measureText(textView.text.toString())
            val startColor = ContextCompat.getColor(this, R.color.home)
            val endColor = ContextCompat.getColor(this, R.color.teal_200)

            val shader = LinearGradient(
                0f, 0f, textWidth, 0f,  // <-- gradient orizzontale: da sinistra a destra
                startColor, // Colore a sinistra
                endColor,   // Colore a destra
                Shader.TileMode.CLAMP
            )
            textView.paint.shader = shader
            textView.invalidate()
        }
        //

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val homeColor = ContextCompat.getColorStateList(this, R.color.nav_home_icon)
        val searchColor = ContextCompat.getColorStateList(this, R.color.nav_search_icon)
        val addColor = ContextCompat.getColorStateList(this, R.color.nav_add_icon)
        val notificationsColor = ContextCompat.getColorStateList(this, R.color.nav_notify_icon)
        val profileColor = ContextCompat.getColorStateList(this, R.color.nav_profile_icon)

        // Setta il ColorStateList prima di selezionare le icone
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    bottomNav.itemIconTintList = homeColor
                    true
                }
                R.id.nav_search -> {
                    bottomNav.itemIconTintList = searchColor
                    true
                }
                R.id.nav_add -> {
                    bottomNav.itemIconTintList = addColor
                    true
                }
                R.id.nav_notifications -> {
                    bottomNav.itemIconTintList = notificationsColor
                    true
                }
                R.id.nav_profile -> {
                    bottomNav.itemIconTintList = profileColor
                    true
                }
                else -> false
            }
        }

    }
}





