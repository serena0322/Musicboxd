package com.example.musicboxd

import retrofit2.Call
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Field

interface SpotifyAuthService {
    @FormUrlEncoded
    @POST("token")
    fun getToken(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Call<TokenResponse>
}
