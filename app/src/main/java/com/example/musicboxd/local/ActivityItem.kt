package com.example.musicboxd.local

import com.google.firebase.Timestamp


data class ActivityItem(
    val action: String = "",
    val timestamp: Timestamp? = null
)
