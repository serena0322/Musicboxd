package com.example.musicboxd.local

import com.google.firebase.Timestamp

data class Review(
    val documentId: String,
    val actionType: String = "",
    val artistName: String = "",
    val songTitle: String = "",
    val sourceUserId: String = "",
    val albumCoverUrl: String = "",
    val rating: Double = 0.0,
    val reviewText: String = "",
    val timestamp: Timestamp? = Timestamp.now()
)

