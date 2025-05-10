package com.example.newcollegeplanner

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory //converter impirt

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://68117ac53ac96f7119a4aa8d.mockapi.io/")
            .addConverterFactory(GsonConverterFactory.create()) //convering into a json
            .build()
    }

    val api: ApiPush by lazy {
        retrofit.create(ApiPush::class.java)
    }
}