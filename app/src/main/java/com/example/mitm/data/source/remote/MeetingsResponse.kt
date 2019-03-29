package com.example.mitm.data.source.remote

import com.example.mitm.data.models.Meeting

data class MeetingsResponse(
        val avatar_url: String,
        val first_name: String,
        val id: Int,
        val last_name: String,
        val my_meetings: List<Meeting>,
        val success: Boolean
)