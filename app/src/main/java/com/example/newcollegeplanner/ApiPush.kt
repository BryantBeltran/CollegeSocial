package com.example.newcollegeplanner

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiPush {
    @POST("/Events")
    fun addEvent(@Body event: Event): Call<Void>
}
