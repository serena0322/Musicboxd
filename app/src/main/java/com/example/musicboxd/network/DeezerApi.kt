package com.example.musicboxd.network

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path


interface DeezerApi {

    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String
    ): DeezerResponse

    @GET("track/{id}")
    suspend fun getTrack(@Path("id") trackId: String): Track

    @GET("album/{id}")
    suspend fun getAlbumDetails(@Path("id") albumId: Long): AlbumDetailsResponse

}


