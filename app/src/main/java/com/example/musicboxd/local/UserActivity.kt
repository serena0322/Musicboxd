package com.example.musicboxd.local

import com.google.firebase.Timestamp

data class UserActivity(
    val actionType: String,
    val sourceUserId: String?,
    val targetUserId: String?,
    val timestamp: Timestamp,
    val songTitle: String? = null,
    val artistName: String? = null
)
