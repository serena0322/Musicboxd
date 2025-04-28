package com.example.musicboxd

import android.annotation.SuppressLint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {
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

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        val homeColor = ContextCompat.getColorStateList(this, R.color.nav_home_icon)
        val searchColor = ContextCompat.getColorStateList(this, R.color.nav_search_icon)
        val addColor = ContextCompat.getColorStateList(this, R.color.nav_add_icon)
        val notificationsColor = ContextCompat.getColorStateList(this, R.color.nav_notify_icon)
        val profileColor = ContextCompat.getColorStateList(this, R.color.nav_profile_icon)

        // Setta il ColorStateList prima di selezionare le icone
        bottomNav.setOnItemSelectedListener()
        {
                item ->
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

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        // Tab Music cliccato
                        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this@MainActivity, R.color.home))
                        null
                    }

                    1 -> {
                        // Tab Reviews cliccato
                        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this@MainActivity, R.color.add))
                        null
                    }

                    2 -> {
                        // Tab Lists cliccato
                        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this@MainActivity, R.color.teal_200))
                        null
                    }

                    3 -> {
                        // Tab Journal cliccato
                        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this@MainActivity, R.color.profile))
                        null
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                TODO("Not yet implemented")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                TODO("Not yet implemented")
            }
        })
    }
}







