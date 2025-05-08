package com.example.musicboxd

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PasswordFragment: Fragment() {
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_password_and_authentication,container,false)
        val change = view.findViewById<TextView>(R.id.changePassword)
        change.setOnClickListener {
            findNavController().navigate(R.id.action_PasswordandAuthentication_to_changePassword)
        }

        return view
    }
}