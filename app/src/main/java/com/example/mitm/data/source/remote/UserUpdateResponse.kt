package com.example.mitm.data.source.remote

data class UserUpdateResponse(
        val avatar_url: String,
        val first_name: String,
        val id: Int,
        val last_name: String,
        val phone: Long,
        val success: Boolean
)