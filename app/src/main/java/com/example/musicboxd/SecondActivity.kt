package com.example.musicboxd

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// Activity di avvio: verifica se l’utente è già autenticato tramite FirebaseAuth e, in caso positivo,
// reindirizza automaticamente alla MainActivity pulendo lo stack delle attività precedenti.

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