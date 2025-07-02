package com.example.musicboxd.remote

//JSON da API Deezer
data class DeezerResponse(val data: List<RemoteTrack>)

data class RemoteTrack(
    val id: Long,
    val title: String,
    val duration: Int,
    val preview: String,
    val artist: RemoteArtist,
    val album: RemoteAlbum
)

data class RemoteArtist(
    val id: Long,
    val name: String
)

data class RemoteAlbum(
    val id: Long,
    val title: String,
    val cover: String
)