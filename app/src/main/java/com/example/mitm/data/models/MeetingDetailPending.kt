package com.example.mitm.data.models

data class MeetingDetailPending(
        val meeting_creator: String,
        val meeting_creator_pic: Any,
        val meeting_description: String,
        val meeting_id: Int,
        val meeting_name: String,
        val meeting_start_date: String,
        val meeting_start_time: String,
        val success: Boolean,
        val user_list: List<User>
)