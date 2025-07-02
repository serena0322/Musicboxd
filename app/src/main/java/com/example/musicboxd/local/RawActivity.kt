package com.example.musicboxd.local

import com.google.firebase.Timestamp

data class RawActivity(
    val actionType: String,
    val sourceUserId: String?,
    val targetUserId: String?,
    val timestamp: Timestamp
)
