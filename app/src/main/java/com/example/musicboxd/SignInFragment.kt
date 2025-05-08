package com.example.musicboxd

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SignInFragment: Fragment() {
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        val emailEditText =view.findViewById<EditText>(R.id.email)
        val usernameEditText = view.findViewById<EditText>(R.id.username)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val registerButton = view.findViewById<Button>(R.id.buttonSignIn)

        registerButton.setOnClickListener {
            val enteredEmail = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                saveNewCredentials(enteredEmail, username, password)
                Toast.makeText(requireContext(), "Registrazione completata", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_signInFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "Inserisci email, username e password", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun saveNewCredentials(enteredEmail: String, username: String, password: String) {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("saved_email", enteredEmail)
            putString("saved_username", username)
            putString("saved_password", password)
            apply()  // Assicura che vengano salvati
        }
    }

}

