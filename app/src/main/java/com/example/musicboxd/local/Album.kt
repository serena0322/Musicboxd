package com.example.musicboxd.local

import androidx.room.PrimaryKey

data class Album (
    @PrimaryKey val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val coverImageUrl: List<Image> = emptyList(),
    val releaseDate: String = "",
    val genres: List<String> = emptyList()
)