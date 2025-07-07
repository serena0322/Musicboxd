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
    suspend fun getTrackById(
        @Path("id") id: String
    ): Track
}
