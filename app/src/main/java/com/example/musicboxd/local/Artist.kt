package com.example.musicboxd.local

import androidx.room.PrimaryKey

data class ArtistEntity (
    @PrimaryKey val id: String = "",
    val name: String = "",
    val genre: String = "",
    val yearStarted: Int? = null,
    val bio: String = "",
    val imageUrl: String? = null
)