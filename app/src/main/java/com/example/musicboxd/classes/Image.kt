package com.example.musicboxd.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity class Image (
    @PrimaryKey val id: String="",
    val url: String? = null,
    val albumId: String = ""
)
