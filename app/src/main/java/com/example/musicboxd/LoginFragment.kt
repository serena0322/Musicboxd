package com.example.musicboxd

import android.annotation.SuppressLint
import android.content.Context
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

private lateinit var usernameEditText: EditText
private lateinit var passwordEditText: EditText
private lateinit var loginButton: Button


class LoginFragment : Fragment(){
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.login_fragment, container, false)

        usernameEditText = view.findViewById(R.id.username)
        passwordEditText = view.findViewById(R.id.password)
        loginButton = view.findViewById(R.id.buttonLogin)

        loginButton.setOnClickListener {
            val enteredIdentifier = usernameEditText.text.toString()
            val enteredPassword = passwordEditText.text.toString()

            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val savedUsername = sharedPreferences.getString("saved_username", null)
            val savedEmail = sharedPreferences.getString("saved_email", null)
            val savedPassword = sharedPreferences.getString("saved_password", null)

            // Confronta enteredIdentifier con username o email
            val isLoginValid = (enteredIdentifier == savedUsername || enteredIdentifier == savedEmail) &&
                    enteredPassword == savedPassword
            if (isLoginValid) {
                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                navigateToHomeScreen()
            } else {
                Toast.makeText(context, "Credenziali errate", Toast.LENGTH_SHORT).show()
            }
        }

        val goToRegister = view.findViewById<TextView>(R.id.textViewSignup)
        goToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signInFragment)
        }

        return view
    }

    private fun navigateToHomeScreen() {
        // Usa requireContext() o requireActivity() per ottenere il contesto
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()  // Chiudi la schermata di login
    }

    //Login automatico al riavvio dell'app + sfumatura nome app
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Login automatico
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        //sfumatura nome app

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