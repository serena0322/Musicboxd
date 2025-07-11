package com.example.musicboxd


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth

class SecondActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onStart() {

        // Controlla se l'utente è già loggato
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Utente già autenticato, passa direttamente alla MainActivity
            navigateToMainActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.second_activity)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            // Pulizia stack per evitare problemi di comportamento e backstack
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}