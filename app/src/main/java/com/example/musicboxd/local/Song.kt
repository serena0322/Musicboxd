package com.example.musicboxd.local

//Salvataggio dati nel DB locale o uso offline

data class Song (
    val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val albumId: String = "",
    val durationSeconds: Int? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null
)