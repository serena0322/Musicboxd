package com.example.musicboxd.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//Passaggio tra schermate, visualizzazione UI
@Parcelize
data class DeezerResponse(val data: List<Track>): Parcelable

@Parcelize
data class Track(
    val title: String?,
    val artist: Artist?,
    val album: Album?,
    val duration: Int?,
    val preview: String?,
    val rank: Int?,
    val explicit_lyrics: Boolean?
): Parcelable

@Parcelize
data class Artist(val name: String?): Parcelable

@Parcelize
data class Album(val title: String?, val cover: String?, val release_date: String?): Parcelable
