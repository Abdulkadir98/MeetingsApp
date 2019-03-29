package com.example.mitm.data.source.remote

data class AuthResponse(

        val message: String,
        val profile_completed: Boolean,
        val success: Boolean,
        val token: String,
        val id: Int
)