package com.example.musicboxd.local

import com.google.firebase.Timestamp

data class ActivityItem(
    val content: String = "",
    val timestamp: Timestamp? = null
)
