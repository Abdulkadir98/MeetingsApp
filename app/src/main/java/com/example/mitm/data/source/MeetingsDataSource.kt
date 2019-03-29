package com.example.mitm.data.source

import com.example.mitm.data.models.Meeting
import com.example.mitm.data.models.MeetingDetail
import com.example.mitm.data.models.MeetingDetailPending
import com.example.mitm.data.source.remote.UserUpdateResponse
import com.example.mitm.data.source.remote.requests.CreateMeetingRequest
import com.example.mitm.data.source.remote.requests.EditMeetingRequest

import com.example.mitm.data.source.remote.requests.InvitationUpdate

import com.example.mitm.data.source.remote.requests.SignUpRequest


interface MeetingsDataSource {

    interface LoadAuthTokenCallback {

        fun onAuthTokenLoaded(token: String)

        fun onDataNotAvailable(error: String)
    }

    interface LoadMeetingsCallback {

        fun onMeetingsLoaded(meetings: List<Meeting>)

        fun onDataNotAvailable(error: String)
    }

    interface LoadMeetingDetailsCallback {

        fun onMeetingDetailsLoaded(meetingDetails: MeetingDetail)

        fun onDataNotAvailable()
    }

    interface LoadMeetingDetailsPendingCallback {

        fun onMeetingDetailsLoaded(meetingDetailsPending: MeetingDetailPending)

        fun onDataNotAvailable()
    }

    interface LoadOtpCallback {

        fun onOtpReceived(otp: Int)

        fun onDataNotAvailable(error: String)
    }

    interface InvitationUpdateCallback {

        fun onStatusUpdateSuccess()

        fun onStatusUpdateFailed()
    }

    interface SaveMeetingCallback {

        fun onMeetingSaved()

        fun onDataNotAvailable()
    }

    interface DeleteMeetingsCallback {

        fun onMeetingDeleteSuccess()

        fun onMeetingDeleteFail()
    }

    interface LoadUserDetailsUpdateCallback {

        fun onDetailsLoaded(user: UserUpdateResponse)

        fun onDataNotAvailable()
    }

    fun deleteUser(token: String)

    fun deleteMeeting(token: String, meetingId: Int, callback: DeleteMeetingsCallback)

    fun editMeeting(token: String, meeting: EditMeetingRequest)

    fun saveMeeting(token: String, meeting: CreateMeetingRequest, callback: SaveMeetingCallback)

    fun loadMeetings(token: String, callback: LoadMeetingsCallback)

    fun loadMeetingDetails(token: String, meetingId: Int, callback: LoadMeetingDetailsCallback)

    fun loadMeetingDetailsPending(token: String, meetingId: Int, callback: LoadMeetingDetailsPendingCallback)

    fun authenticateUser(phoneNumber: Long, otp: Int, callback: LoadAuthTokenCallback)

    fun getOTP(phoneNumber: Long, callback: LoadOtpCallback)

    fun acceptOrDeclineMeeting(token: String, body: InvitationUpdate, callback: InvitationUpdateCallback)

    fun signUp(token: String,meeting: SignUpRequest, callback: MeetingsDataSource.LoadUserDetailsUpdateCallback)

}