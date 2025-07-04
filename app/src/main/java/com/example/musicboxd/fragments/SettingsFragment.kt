package com.example.musicboxd.fragments

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
import androidx.lifecycle.lifecycleScope
import com.example.musicboxd.SecondActivity
import com.example.musicboxd.R
import androidx.navigation.fragment.findNavController
import com.example.musicboxd.`object`.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class SettingsFragment : Fragment() {

    private lateinit var username: TextView
    private lateinit var firstName: TextView
    private lateinit var lastName: TextView
    private lateinit var email: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI binding
        username = view.findViewById(R.id.username)
        firstName = view.findViewById(R.id.firstName)
        lastName = view.findViewById(R.id.lastName)
        email = view.findViewById(R.id.email)

        // Osserva LiveData per aggiornare UI
        UserRepository.currentUser.observe(viewLifecycleOwner) { userWithActivities ->
            val user = userWithActivities?.user
            user?.let {
                username.text = "Signed in as ${it.username}"
                firstName.text = "First name: ${it.firstName}"
                lastName.text = "Last name: ${it.lastName}"
                email.text = "Email: ${it.email}"
            }
        }

        // Click listeners per aggiornare campi
        username.setOnClickListener {
            showInputDialog("Insert your username", "Enter your username", "username") {
                username.text = "Signed in as $it"
            }
        }

        firstName.setOnClickListener {
            showInputDialog("Insert your name", "Enter your first name", "firstName") {
                firstName.text = "First name: $it"
            }
        }

        lastName.setOnClickListener {
            showInputDialog("Insert your last name", "Enter your last name", "lastName") {
                lastName.text = "Last name: $it"
            }
        }

        // Logout
        view.findViewById<TextView>(R.id.signOut).setOnClickListener {
            showConfirmDialog("Esci", "Sei sicuro di voler uscire?") {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireContext(), SecondActivity::class.java))
                requireActivity().finish()
            }
        }

        // Eliminazione account
        view.findViewById<TextView>(R.id.cancel).setOnClickListener {
            showConfirmDialog(
                "Elimina account",
                "Sei sicuro di voler eliminare il tuo account? Questa operazione è irreversibile."
            ) {
                deleteUser()
            }
        }

        // Sicurezza
        view.findViewById<TextView>(R.id.security).setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_PasswordandAuthentication)
        }
    }

    private fun showInputDialog(
        title: String,
        hint: String,
        field: String,
        onUpdateTextView: (String) -> Unit
    ) {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            this.hint = hint
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("OK") { dialog, _ ->
                val newValue = editText.text.toString()
                if (newValue.isNotBlank()) {
                    UserRepository.updateField(field, newValue, {
                        onUpdateTextView(newValue)
                        Toast.makeText(requireContext(), "$field aggiornato", Toast.LENGTH_SHORT).show()
                    }, {
                        Toast.makeText(requireContext(), "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
                    })
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annulla") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Sì") { _, _ -> onConfirm() }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun deleteUser() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        if (uid != null) {
            FirebaseFirestore.getInstance().collection("User").document(uid).delete()
        }

        auth.currentUser?.delete()
            ?.addOnSuccessListener {
                requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                Toast.makeText(requireContext(), "Account eliminato", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), SecondActivity::class.java))
                requireActivity().finish()
            }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(), "Errore: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
