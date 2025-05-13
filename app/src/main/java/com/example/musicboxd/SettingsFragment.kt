package com.example.musicboxd

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
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

class SettingsFragment: Fragment() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val username = view.findViewById<TextView>(R.id.signedInText)
        // Recupera l'username da SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("saved_username", "Guest")  // "Guest" è il valore predefinito nel caso non sia stato salvato nessun username
        username.text = "Signed in as $savedUsername"
        //Set informazioni personali
        val email = view.findViewById<TextView>(R.id.email)
        val lastName = view.findViewById<TextView>(R.id.last_name)
        val firstName = view.findViewById<TextView>(R.id.first_name)
        firstName.setOnClickListener {
            val editText = EditText(requireContext())
            editText.inputType = InputType.TYPE_CLASS_TEXT
            editText.hint = "Enter your first name"

            AlertDialog.Builder(requireContext())
                .setTitle("Insert your name")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                    val inputName = editText.text.toString()
                    if (inputName.isNotBlank()) {
                        firstName.text = "First name: $inputName"
                        sharedPreferences.edit().putString("first_name", inputName).apply()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
        lastName.setOnClickListener {
            val editText = EditText(requireContext())
            editText.inputType = InputType.TYPE_CLASS_TEXT
            editText.hint = "Enter your last name"

            AlertDialog.Builder(requireContext())
                .setTitle("Insert your last name")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                    val inputName = editText.text.toString()
                    if (inputName.isNotBlank()) {
                        lastName.text = "Last name: $inputName"
                        sharedPreferences.edit().putString("last_name", inputName).apply()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
        email.setOnClickListener {
            val editText = EditText(requireContext())
            editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            // Recupera l'email salvata nelle SharedPreferences
            val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val savedEmail = sharedPref.getString("saved_email", "") ?: ""
            editText.setText(savedEmail)

            AlertDialog.Builder(requireContext())
                .setTitle("Edit your email")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                    val newEmail = editText.text.toString()
                    if (newEmail.isNotBlank()) {
                        // Salva la nuova email
                        sharedPref.edit().putString("saved_email", newEmail).apply()
                        email.text = "Email: $newEmail"
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
        val savedFirstName = sharedPreferences.getString("first_name", null)
        val savedLastName = sharedPreferences.getString("last_name", null)
        val savedEmail = sharedPreferences.getString("saved_email", null)

        if (!savedFirstName.isNullOrEmpty()) {
            firstName.text = "First name: $savedFirstName"
        }
        if (!savedLastName.isNullOrEmpty()) {
            lastName.text = "Last name: $savedLastName"
        }
        if (!savedEmail.isNullOrEmpty()) {
            email.text = "Email: $savedEmail"
        }

        //Logout
        val logout = view.findViewById<TextView>(R.id.signOut)
        logout.setOnClickListener {
            // Cancella solo lo stato di login, ma lascia username e password
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            AlertDialog.Builder(requireContext())
                .setTitle("Esci")
                .setMessage("Sei sicuro di voler uscire?")
                .setPositiveButton("Sì") { dialog, _ ->
                    // Rimuovi solo lo stato di login
                    editor.remove("isLoggedIn")
                    editor.apply()
                    // Reindirizza l'utente alla schermata di login
                    val intent = Intent(requireContext(), SecondActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish() // Chiude l'attività corrente per evitare che l'utente torni indietro
                }
                .setNegativeButton("Annulla") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        //Eliminazione Account
        val deactivate = view.findViewById<TextView>(R.id.cancel)
        deactivate.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

            AlertDialog.Builder(requireContext())
                .setTitle("Elimina account")
                .setMessage("Sei sicuro di voler eliminare il tuo account? Questa operazione è irreversibile.")
                .setPositiveButton("Sì") { dialog, _ ->
                    sharedPref.edit().clear().apply() // Elimina tutti i dati
                    Toast.makeText(requireContext(), "Account eliminato", Toast.LENGTH_SHORT).show()

                    // Torna al login o chiudi l'app
                    val intent = Intent(requireContext(), SecondActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Annulla") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        val authentication = view.findViewById<TextView>(R.id.security)
        authentication.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_PasswordandAuthentication)
        }
        return view
    }
}

