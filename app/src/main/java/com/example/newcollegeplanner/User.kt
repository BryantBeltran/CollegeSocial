package com.example.newcollegeplanner

import com.google.gson.annotations.SerializedName

data class User(
    val id: String, // Matches "id" from MockAPI.io
    val username: String, // Matches "username" from MockAPI.io
    val password: String, // Matches "password" from MockAPI.io
    val email: String,    // Matches "email" from MockAPI.io
    val fullName: String, // Matches "fullName" from MockAPI.io
    val age: Int,         // Changed to Int, assuming "age" will be a number
    val location: String, // Matches "location" from MockAPI.io
    val role: String,     // Matches "role" from MockAPI.io
    val phone: String,    // Matches "phone" from MockAPI.io
    val accountCreated: String, // Matches "accountCreated" from MockAPI.io
    val profilePicture: String  
)