package com.example.musicboxd

data class SearchResponse(val tracks: Tracks)
data class Tracks(val items: List<Track>)
data class Track(
    val name: String,
    val artists: List<Artist>,
    val album: Album
)

data class Artist(val name: String)
data class Album(val name: String, val images: List<Image>)
data class Image(val url: String)
