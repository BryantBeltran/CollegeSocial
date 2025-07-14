package com.example.newcollegeplanner

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Body 
import retrofit2.http.Path

interface ApiService {
    @GET("/Users")
    fun getUsers(): Call<List<User>>

    @GET("/Events")
    fun getEvents(): Call<List<Event>>

    @POST("/Events") // Use POST to create a new resource on the "events" endpoint
    fun addEvent(@Body event: Event): Call<Void> // @Body sends the Event object as JSON, Call<Void> means no specific response body is expected

    @DELETE("/Events/{id}") // DELETE endpoint for removing events
    fun deleteEvent(@Path("id") eventId: String): Call<Void>
}