package com.example.musicboxd.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artist")
data class Artist(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val genre: String = "",
    val yearStarted: Int? = null,
    val bio: String = "",
    val imageUrl: String? = null
)