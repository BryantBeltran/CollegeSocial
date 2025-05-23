package com.example.newcollegeplanner



data class Event(
    val event_name: String,
    val date: String,
    val time: String,
    val location: String,
    val id: String? = null // MockAPI.io adds an ID, make it nullable for POST
)