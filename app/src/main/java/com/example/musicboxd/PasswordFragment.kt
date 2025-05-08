package com.example.musicboxd

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PasswordFragment: Fragment() {
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password_,container,false)
        val change = view.findViewById<TextView>(R.id.changePassword)
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedPassword = sharedPreferences.getString("saved_password", "Guest")
        change.setOnClickListener {
            val inflater = LayoutInflater.from(requireContext())
            val dialogView = inflater.inflate(R.layout.dialog_change_password, null)

            // Recupera i campi dal layout
            val currentPassw = dialogView.findViewById<EditText>(R.id.editCurrentPassword)
            val newPassw = dialogView.findViewById<EditText>(R.id.editNewPassword)
            val confirmNewPassw = dialogView.findViewById<EditText>(R.id.editConfirmPassword)

            // Mostra il dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("OK") { dialog, _ ->
                    var insertedCurrentPassword = currentPassw.text.toString()
                    var insertedNewPassword = newPassw.text.toString()
                    var insertedConfirmPassword = confirmNewPassw.text.toString()
                    when {
                        insertedCurrentPassword != savedPassword -> {
                            Toast.makeText(
                                requireContext(),
                                "La password attuale non è corretta",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        insertedNewPassword != insertedConfirmPassword -> {
                            Toast.makeText(
                                requireContext(),
                                "Le nuove password non coincidono",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        insertedNewPassword == savedPassword -> {
                            Toast.makeText(
                                requireContext(),
                                "La nuova password non può essere uguale alla precedente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        insertedNewPassword.length < 6 -> {
                            Toast.makeText(
                                requireContext(),
                                "La nuova password deve contenere almeno 6 caratteri",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            sharedPreferences.edit()
                                .putString("saved_password", insertedNewPassword).apply()
                            Toast.makeText(
                                requireContext(),
                                "Password aggiornata con successo",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.action_changePassword_to_settingsFragment)
                            // Reset variabili
                            insertedCurrentPassword = ""
                            insertedNewPassword = ""
                            insertedConfirmPassword = ""
                            dialog.dismiss()

                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
        .show()
        }
        return view
    }
}
