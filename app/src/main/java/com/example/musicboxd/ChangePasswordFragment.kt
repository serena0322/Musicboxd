package com.example.musicboxd

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ChangePasswordFragment : Fragment() {

    private var insertedCurrentPassword: String = ""
    private var insertedNewPassword: String = ""
    private var insertedConfirmPassword: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)

        val currentPass = view.findViewById<TextView>(R.id.currentPassword)
        val newPassword = view.findViewById<TextView>(R.id.newPassword)
        val confirmNewPassword = view.findViewById<TextView>(R.id.confirmNewPassword)
        val changePasswordButton = view.findViewById<Button>(R.id.done)
        //recupero password salvata
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedPassword = sharedPreferences.getString("saved_password", "Guest")

        currentPass.setOnClickListener {
            val editText = EditText(requireContext())
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.hint = "Enter your current password"

            AlertDialog.Builder(requireContext())
                .setTitle("Insert your current password")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                    insertedCurrentPassword = editText.text.toString()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
        newPassword.setOnClickListener {
            val editText = EditText(requireContext())
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.hint = "Enter new password"

            AlertDialog.Builder(requireContext())
                .setTitle("Insert new password")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                    insertedNewPassword = editText.text.toString()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
        confirmNewPassword.setOnClickListener {
            val editText = EditText(requireContext())
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.hint = "Confirm new password"

            AlertDialog.Builder(requireContext())
                .setTitle("Confirm new password")
                .setView(editText)
                .setPositiveButton("OK") { dialog, _ ->
                    insertedConfirmPassword = editText.text.toString()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }

        changePasswordButton.setOnClickListener {
            when {
                insertedCurrentPassword != savedPassword -> {
                    Toast.makeText(requireContext(), "La password attuale non è corretta", Toast.LENGTH_SHORT).show()
                }
                insertedNewPassword != insertedConfirmPassword -> {
                    Toast.makeText(requireContext(), "Le nuove password non coincidono", Toast.LENGTH_SHORT).show()
                }
                insertedNewPassword == savedPassword -> {
                    Toast.makeText(requireContext(), "La nuova password non può essere uguale alla precedente", Toast.LENGTH_SHORT).show()
                }
                insertedNewPassword.length < 6 -> {
                    Toast.makeText(requireContext(), "La nuova password deve contenere almeno 6 caratteri", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    sharedPreferences.edit().putString("saved_password", insertedNewPassword).apply()
                    Toast.makeText(requireContext(), "Password aggiornata con successo", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_changePassword_to_settingsFragment)
                    // Reset variabili
                    insertedCurrentPassword = ""
                    insertedNewPassword = ""
                    insertedConfirmPassword = ""
                }
            }
        }

        return view
    }
}
