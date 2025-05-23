package com.example.newcollegeplanner

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST // <--- ADD THIS IMPORT
import retrofit2.http.Body // <--- ADD THIS IMPORT

interface ApiService {
    @GET("/Users")
    fun getUsers(): Call<List<User>>

    @GET("/Events")
    fun getEvents(): Call<List<Event>>


    @POST("/Events") // Use POST to create a new resource on the "events" endpoint
    fun addEvent(@Body event: Event): Call<Void> // @Body sends the Event object as JSON, Call<Void> means no specific response body is expected

}