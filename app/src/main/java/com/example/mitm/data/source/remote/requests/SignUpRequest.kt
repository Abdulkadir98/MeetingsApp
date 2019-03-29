package com.example.mitm.data.source.remote.requests

data class SignUpRequest(
        val user: User

)
data class User(
        var avatar_url: String,
        var first_name: String,
        var last_name: String
)