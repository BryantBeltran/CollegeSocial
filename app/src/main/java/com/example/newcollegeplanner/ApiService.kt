package com.example.newcollegeplanner

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/Events")
    fun getEvents(): Call<List<Event>>
}