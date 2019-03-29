package com.example.mitm.data.source.remote.requests

data class EditMeetingRequest(
        var description: String,
        var duration: Int,
        var invited_phone: List<Long>,
        var name: String,
        var start_date_time: String,
        val meeting_id: Int)