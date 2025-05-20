package com.example.musicboxd.fragments

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
import com.example.musicboxd.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class PasswordFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password_, container, false)
        val change = view.findViewById<TextView>(R.id.changePassword)
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) //per memorrizz locale
        val savedPassword = sharedPreferences.getString("saved_password", "Guest")

        change.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null)

            val currentPassw = dialogView.findViewById<EditText>(R.id.editCurrentPassword)
            val newPassw = dialogView.findViewById<EditText>(R.id.editNewPassword)
            val confirmNewPassw = dialogView.findViewById<EditText>(R.id.editConfirmPassword)

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Cambia Password")
                .setView(dialogView)
                .setPositiveButton("OK", null) // lo gestiamo manualmente per evitare chiusure automatiche
                .setNegativeButton("Annulla") { dialog, _ -> dialog.dismiss() }
                .create()

            dialog.setOnShowListener {
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    val insertedCurrentPassword = currentPassw.text.toString()
                    val insertedNewPassword = newPassw.text.toString()
                    val insertedConfirmPassword = confirmNewPassw.text.toString()

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
                            val user = FirebaseAuth.getInstance().currentUser
                            val email = user?.email

                            if (user != null && email != null) {
                                val credential = EmailAuthProvider.getCredential(email, insertedCurrentPassword)

                                user.reauthenticate(credential)
                                    .addOnSuccessListener {
                                        user.updatePassword(insertedNewPassword)
                                            .addOnSuccessListener {
                                                sharedPreferences.edit() //per memorrizz locale
                                                    .putString("saved_password", insertedNewPassword)
                                                    .apply()

                                                Toast.makeText(requireContext(), "Password aggiornata con successo", Toast.LENGTH_SHORT).show()
                                                dialog.dismiss()
                                                findNavController().navigate(R.id.action_changePassword_to_settingsFragment)
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(requireContext(), "Errore nell'aggiornamento della password", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Password attuale errata o sessione scaduta", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(requireContext(), "Utente non autenticato", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            dialog.show()
        }
        return view
    }
}
