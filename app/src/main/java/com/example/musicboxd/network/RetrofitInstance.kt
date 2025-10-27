package com.example.musicboxd.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Oggetto singleton che inizializza Retrofit con base URL di Deezer e GsonConverterFactory,
// fornendo l’istanza dell’interfaccia DeezerApi per le chiamate di rete.

object RetrofitInstance {
    val api: DeezerApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeezerApi::class.java)
    }
}
