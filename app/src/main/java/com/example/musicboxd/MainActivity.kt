package com.example.musicboxd

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView

class MainActivity() : AppCompatActivity(), Parcelable {
    @SuppressLint("MissingInflatedId")
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    constructor(parcel: Parcel) : this() {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                    bottomNav.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_add_icon)
                    navController.navigate(R.id.addFragment)
                    true
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
        // Trova la RecyclerView e la SearchView nel layout
        recyclerView = findViewById(R.id.scrollView2)
        searchView = findViewById(R.id.searchView)

        // Inizialmente nascondi la RecyclerView
        recyclerView.visibility = View.GONE

        // Aggiungi un listener alla SearchView per rilevare quando l'utente inizia a digitare
        searchView.setOnQueryTextListener(/* listener = */ object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Gestisci la logica di ricerca quando l'utente invia la query
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Rendi visibile la RecyclerView quando l'utente inizia a digitare
                if (newText.isNullOrEmpty()) {
                    recyclerView.visibility = View.GONE  // Nascondi se la query è vuota
                } else {
                    recyclerView.visibility = View.VISIBLE  // Mostra se c'è del testo
                }
                return true
            }
         })
    }
}






