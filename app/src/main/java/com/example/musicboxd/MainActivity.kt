package com.example.musicboxd

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment
import com.example.musicboxd.fragments.AddSongBottomSheet
import com.example.musicboxd.`object`.UserRepository
import kotlinx.coroutines.launch

class MainActivity() : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            UserRepository.loadUser()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val destination = intent.getStringExtra("destination")

        // Trova il NavHostFragment e ottieni il NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        if (destination == "home") {
            navController.navigate(R.id.homeFragment)
        }


        // Collega la bottom navigation al NavController
        bottomNav.setupWithNavController(navController)

        // Setta il ColorStateList prima di selezionare le icone
        bottomNav.setOnItemSelectedListener()
        { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    bottomNav.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_home_icon)
                    navController.navigate(R.id.homeFragment)
                    true
                }

                R.id.nav_search -> {
                    bottomNav.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_search_icon)
                    navController.navigate(R.id.searchFragment)
                    true
                }

                R.id.nav_add -> {
                    bottomNav.itemIconTintList =
                        ContextCompat.getColorStateList(this, R.color.nav_add_icon)
                    // Mostra la Bottom Sheet invece di cambiare fragment
                    val bottomSheet = AddSongBottomSheet()
                    bottomSheet.show(supportFragmentManager, "BottomSheetAddSong")

                    false // NON cambiamo fragment
                }

                R.id.nav_activity -> {
                    bottomNav.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_activity_icon)
                    navController.navigate(R.id.activityFragment)
                    true
                }

                R.id.nav_profile -> {
                    bottomNav.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_profile_icon)
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }
    }
}






