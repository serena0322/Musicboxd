package com.example.musicboxd.local


data class PlaylistItem(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val tracks: List<String> = emptyList()
)

