package com.example.mitm.data.source.remote.requests

data class InvitationUpdate(
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        val meeting_id: Int,
        var status: String
)