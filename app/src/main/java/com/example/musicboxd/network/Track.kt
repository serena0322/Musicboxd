package com.example.musicboxd.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeezerResponse(val data: List<Track>): Parcelable

@Parcelize
data class Track(
    val id: Long?,
    val title: String?,
    val artist: Artist?,
    val album: Album?,
    val duration: Int?,
    val preview: String?,
): Parcelable

@Parcelize
data class Artist(val name: String?): Parcelable

@Parcelize
data class Album(
    val id: Long?,
    val title: String?,
    val genres: GenreResponse?,
    val cover: String?,
    val releaseDate: String?
) : Parcelable


@Parcelize
data class AlbumDetailsResponse(
    val id: Long?,
    val title: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    val cover: String?,
    val genres: GenreResponse?
) : Parcelable


@Parcelize
data class GenreResponse(
    val data: List<Genre>?
) : Parcelable

@Parcelize
data class Genre(
    val id: Long?,
    val name: String?
) : Parcelable
