package com.example.mitm.data.source.remote

data class MyMeeting(
        val accepted: Int,
        val duration: Int,
        val invited: Int,
        val meeting_creator: String,
        val meeting_creator_pic: String,
        val meeting_id: Int,
        val meeting_name: String,
        val meeting_start_date: String,
        val meeting_start_time: String,
        val meeting_status: String,
        val profile_pics: List<Any>
)