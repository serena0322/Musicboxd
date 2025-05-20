package com.example.musicboxd.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.musicboxd.MainActivity
import com.example.musicboxd.R
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment(){
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        emailEditText = view.findViewById(R.id.email)
        passwordEditText = view.findViewById(R.id.password)
        loginButton = view.findViewById(R.id.buttonLogin)

        val goToRegister = view.findViewById<TextView>(R.id.textViewSignup)

        loginButton.setOnClickListener {
            val enteredEmail = emailEditText.text.toString()
            val enteredPassword = passwordEditText.text.toString()
            val auth = FirebaseAuth.getInstance()

            auth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        navigateToHomeScreen()
                    } else {
                        Toast.makeText(context, "Credenziali errate", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        goToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signInFragment)
        }
        return view
    }

    private fun navigateToHomeScreen() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()  // Chiudi la schermata di login
    }


    // sfumatura nome app
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inizializzazione del TextView dopo l'inflazione e controllo su null
        val textView = view.findViewById<TextView>(R.id.Title)

        // Utilizzo di post() per eseguire codice sulla UI dopo che la vista è pronta
        textView?.post {
            // Misura la larghezza del testo
            val textWidth = textView.paint.measureText(textView.text.toString())

            // Colori per la sfumatura
            val startColor = ContextCompat.getColor(requireContext(), R.color.home)
            val endColor = ContextCompat.getColor(requireContext(), R.color.teal_200)

            // Creazione della sfumatura orizzontale
            val shader = LinearGradient(
                0f, 0f, textWidth, 0f,  // Sfumatura orizzontale (da sinistra a destra)
                startColor,
                endColor,
                Shader.TileMode.CLAMP
            )

            // Applicazione della sfumatura al TextView
            textView.paint.shader = shader
            textView.invalidate()
        }
    }
}