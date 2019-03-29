package com.example.mitm.data.models

data class MeetingDetail(
        val latitude: String,
        val longitude: String,
        val meeting_creator: String,
        val meeting_creator_pic: Any,
        val meeting_description: String,
        val meeting_id: Int,
        val meeting_name: String,
        val duration: Double,
        val meeting_start_date: String,
        val meeting_start_time: String,
        val places: List<Place>,
        val status: String,
        val success: Boolean,
        val user_list: List<User>
)

data class User(
        val avatar_url: String,
        val first_name: String,
        val last_name: String,
        val phone: Long,
        val status: Int
)

data class Place(
        val distance_in_kms: Double,
        val icon: String,
        val latitude: Double,
        val longitude: Double,
        val name: String,
        val rating: Any
)