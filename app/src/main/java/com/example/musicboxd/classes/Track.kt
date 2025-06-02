package com.example.musicboxd.classes

data class DeezerResponse(val data: List<Track>)

data class Track(
    val title: String,
    val artist: Artist,
    val album: Album
)

data class Artist(val name: String)
data class Album(val title: String, val cover: String)
