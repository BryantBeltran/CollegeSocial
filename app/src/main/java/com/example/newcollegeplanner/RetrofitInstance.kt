package com.example.newcollegeplanner

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory //converter impirt

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/") // <-- IMPORTANT: UPDATED BASE URL
            .addConverterFactory(GsonConverterFactory.create()) //convering into a json
            .build()
    }

    // Assuming ApiPush is actually meant to be your ApiService interface for events and users
    // If ApiPush is a completely separate API, let me know.
    val api: ApiService by lazy { // <-- CHANGED from ApiPush to ApiService
        retrofit.create(ApiService::class.java)
    }
}