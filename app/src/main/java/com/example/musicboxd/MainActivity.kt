package com.example.musicboxd

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.musicboxd.fragments.AddSongBottomSheet
import kotlinx.coroutines.launch
import com.example.musicboxd.viewModels.UserViewModel
import com.google.firebase.FirebaseApp
import kotlin.math.max

// Activity principale: inizializza Firebase e UserViewModel, carica/Osserva profilo e imposta NavHost + BottomNavigation (Home, Search, Activity, Profile) con tint dinamico.
// Gestisce l’azione “Add” aprendo la BottomSheet AddSong invece di navigare, e consente deep-link iniziale via extra "destination".

class MainActivity() : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()

    // helper dp
    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomBarContainer = findViewById<View>(R.id.bottom_bar_container)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

// Disattiva la pillola M3/MDC
        bottomNav.setItemActiveIndicatorEnabled(false)
// facoltativo: nessun colore assegnato
        bottomNav.itemActiveIndicatorColor = null

        // edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<View>(R.id.root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // padding laterali 16dp; margine top/bottom dinamici (status/gesture)
            v.setPadding(10.dp, bars.top + 12.dp, 10.dp, bars.bottom + 24.dp)
            WindowInsetsCompat.CONSUMED
        }

        // Gesture bar / tastiera: margine bottom dinamico sul contenitore
        ViewCompat.setOnApplyWindowInsetsListener(bottomBarContainer) { v, insets ->
            val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val extra = max(sysBottom, imeBottom)
            val base = (6 * resources.displayMetrics.density).toInt() // margine estetico
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = base + extra }
            insets
        }

        // NavController + listener (Add apre la BottomSheet)
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        if (bottomNav.menu.size() == 0) bottomNav.inflateMenu(R.menu.bottom_nav_menu)
        if (bottomNav.selectedItemId == 0) bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> { AddSongBottomSheet().show(supportFragmentManager, "AddSong"); false }
                else -> NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
        bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.nav_add) AddSongBottomSheet().show(supportFragmentManager, "AddSong")
        }

        // Carica dati utente e attività all'avvio
        lifecycleScope.launch {
            userViewModel.loadMyBasicProfile()
            userViewModel.observeMyUserRealtime() //aggiorna dati in real time
            userViewModel.observeMyProfileDataRealtime()
        }

        val app = FirebaseApp.getInstance()
        val opt = app.options
        Log.d("FirebaseProjectCheck", "projectId=${opt.projectId}, appId=${opt.applicationId}, apiKey=${opt.apiKey}, storage=${opt.storageBucket}")


        val destination = intent.getStringExtra("destination")

        // Trova il NavHostFragment e ottieni il NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment


        if (destination == "home") {
            navController.navigate(R.id.homeFragment)
        }

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
                    userViewModel.loadMyAndFriendsActivities()
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

    override fun attachBaseContext(newBase: Context) {
        val config = newBase.resources.configuration

        // Consente ridimensionamento ma entro limiti sicuri
        val clamped = config.fontScale.coerceIn(1.0f, 1.20f)
        if (config.fontScale != clamped) {
            config.fontScale = clamped
        }

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}






