package com.example.musicboxd

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.login_fragment, container, false)

        val goToRegister = view.findViewById<TextView>(R.id.textViewSignup)
        goToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signInFragment)
        }

        val skip = view.findViewById<Button>(R.id.button)
        skip.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("destination", "home")
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }


    //Sfumatura nome app
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