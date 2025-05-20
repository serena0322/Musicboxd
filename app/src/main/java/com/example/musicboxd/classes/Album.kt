package com.example.musicboxd.classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp


@Entity class Album(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val coverImageUrl: List<Image> = emptyList(),
    val releaseDate: String = "",
    val genres: List<String> = emptyList()
)
