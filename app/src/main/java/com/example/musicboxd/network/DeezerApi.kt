package com.example.musicboxd.network

import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String
    ): DeezerResponse
}
