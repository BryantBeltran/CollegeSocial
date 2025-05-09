package com.example.newcollegeplanner

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/")  // Make sure this ends with /
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiPush by lazy {
        retrofit.create(ApiPush::class.java)
    }
}