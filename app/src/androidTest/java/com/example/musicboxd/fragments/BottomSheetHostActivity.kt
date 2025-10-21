package com.example.musicboxd.fragments

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class BottomSheetHostActivity : AppCompatActivity() {
    companion object { const val CONTAINER_ID = 0xB00B135.toInt() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = FrameLayout(this).apply { id = CONTAINER_ID }
        setContentView(container)
    }
}
