package com.example.musicboxd

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SettingsFragment: Fragment() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // UI
        val username = view.findViewById<TextView>(R.id.username)
        val firstName = view.findViewById<TextView>(R.id.firstName)
        val lastName = view.findViewById<TextView>(R.id.lastName)
        val email = view.findViewById<TextView>(R.id.email)

        // Inizializzazione Firebase
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        //Per ottenere l'ID dell’utente
        val currentUser = auth.currentUser
        //accedere al suo uid
        val userDocRef = firestore.collection("users").document(currentUser!!.uid)
        username.text = "Signed in as ${currentUser.email}"

        // Recupera i dati da Firestore
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                firstName.text = "First name: ${document.getString("firstName") ?: ""}"
                lastName.text = "Last name: ${document.getString("lastName") ?: ""}"
                email.text = "Email: ${document.getString("email") ?: currentUser.email}"
            }
        }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore nel recupero dati: ${it.message}", Toast.LENGTH_SHORT).show()
            }


        // Aggiornamento nome
        firstName.setOnClickListener {
            val editText = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT
                hint = "Enter your first name"
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Insert your name")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                    val name = editText.text.toString()
                    if (name.isNotBlank()) {
                        firstName.text = "First name: $name"
                        userDocRef.set(mapOf("firstName" to name), SetOptions.merge())
                        Toast.makeText(requireContext(), "Nome aggiornato", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .show()
        }

        // Aggiornamento cognome
        lastName.setOnClickListener {
            val editText = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT
                hint = "Enter your last name"
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Insert your last name")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                    val surname = editText.text.toString()
                    if (surname.isNotBlank()) {
                        lastName.text = "Last name: $surname"
                        userDocRef.set(mapOf("lastName" to surname), SetOptions.merge())
                        Toast.makeText(requireContext(), "Cognome aggiornato", Toast.LENGTH_SHORT).show()

                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .show()
        }

        // Logout
        view.findViewById<TextView>(R.id.signOut).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Esci")
                .setMessage("Sei sicuro di voler uscire?")
                .setPositiveButton("Sì") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(requireContext(), SecondActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        // Eliminazione account
        view.findViewById<TextView>(R.id.cancel).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Elimina account")
                .setMessage("Sei sicuro di voler eliminare il tuo account? Questa operazione è irreversibile.")
                .setPositiveButton("Sì") { _, _ ->
                    userDocRef.delete()
                    currentUser.delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Account eliminato", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(requireContext(), SecondActivity::class.java))
                            requireActivity().finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        // Sicurezza
        view.findViewById<TextView>(R.id.security).setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_PasswordandAuthentication)
        }

        return view
    }
}

