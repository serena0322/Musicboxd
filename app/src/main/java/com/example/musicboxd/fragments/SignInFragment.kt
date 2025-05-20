package com.example.musicboxd.fragments

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
import com.example.musicboxd.R
import com.example.musicboxd.classes.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SignInFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        val emailEditText = view.findViewById<EditText>(R.id.email)
        val usernameEditText = view.findViewById<EditText>(R.id.username)  // Corretto qui
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val registerButton = view.findViewById<Button>(R.id.buttonSignIn)
        val auth = FirebaseAuth.getInstance()

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci email, username e password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        val user = User(
                            id = firebaseUser?.uid ?: "",
                            username = username,
                            email = email,
                            picture = "",
                            bio = "",
                            followersCount = 0,
                            followingCount = 0,
                            createdAt = Timestamp.now()
                        )
                        // Salvataggio su Firestore
                        FirebaseFirestore.getInstance().collection("User")
                            .document(user.id)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Registrazione completata", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_signInFragment_to_loginFragment)
                                // Dopo il successo nel salvataggio su Firestore:
                                saveNewCredentials(email, username, password)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Errore durante la registrazione", Toast.LENGTH_SHORT).show()
                            }

                        // Se vuoi salvare le credenziali localmente
                        // saveNewCredentials(email, username, password)

                    } else {
                        Toast.makeText(requireContext(), "Errore: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
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
            apply()
        }
    }
}



