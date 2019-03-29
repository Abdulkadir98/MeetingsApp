package com.example.mitm.data.source

import com.example.mitm.data.models.Meeting
import com.example.mitm.data.models.MeetingDetail
import com.example.mitm.data.models.MeetingDetailPending
import com.example.mitm.data.source.remote.UserUpdateResponse
import com.example.mitm.data.source.remote.requests.CreateMeetingRequest
import com.example.mitm.data.source.remote.requests.EditMeetingRequest

import com.example.mitm.data.source.remote.requests.InvitationUpdate

import com.example.mitm.data.source.remote.requests.SignUpRequest



class MeetingsRepository(val remoteMeetingsDataSource: MeetingsDataSource) : MeetingsDataSource {


    companion object {
        private var INSTANCE: MeetingsRepository? = null

        @JvmStatic
        fun getInstance(foodRemoteDataSource: MeetingsDataSource): MeetingsRepository {
            return INSTANCE ?: MeetingsRepository(foodRemoteDataSource)
                    .apply { INSTANCE = this }
        }
    }

    override fun acceptOrDeclineMeeting(token: String, body: InvitationUpdate, callback: MeetingsDataSource.InvitationUpdateCallback) {

        remoteMeetingsDataSource.acceptOrDeclineMeeting(token, body, object : MeetingsDataSource.InvitationUpdateCallback {
            override fun onStatusUpdateSuccess() {
                callback.onStatusUpdateSuccess()
            }

            override fun onStatusUpdateFailed() {
                callback.onStatusUpdateFailed()
            }

        })
    }



    override fun saveMeeting(token: String, meeting: CreateMeetingRequest, callback: MeetingsDataSource.SaveMeetingCallback) {

        remoteMeetingsDataSource.saveMeeting(token, meeting, object : MeetingsDataSource.SaveMeetingCallback {
            override fun onMeetingSaved() {
                callback.onMeetingSaved()
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }

        })
    }

    override fun loadMeetings(token: String, callback: MeetingsDataSource.LoadMeetingsCallback) {

        remoteMeetingsDataSource.loadMeetings(token, object : MeetingsDataSource.LoadMeetingsCallback {
            override fun onMeetingsLoaded(meetings: List<Meeting>) {

                callback.onMeetingsLoaded(meetings)
            }

            override fun onDataNotAvailable(error: String) {

                callback.onDataNotAvailable(error)
            }

        })
    }

    override fun loadMeetingDetails(token: String, meetingId: Int, callback: MeetingsDataSource.LoadMeetingDetailsCallback) {

        remoteMeetingsDataSource.loadMeetingDetails(token, meetingId, object : MeetingsDataSource.LoadMeetingDetailsCallback{
            override fun onMeetingDetailsLoaded(meetingDetails: MeetingDetail) {

                callback.onMeetingDetailsLoaded(meetingDetails)
            }

            override fun onDataNotAvailable() {

                callback.onDataNotAvailable()
            }

        })
    }

    override fun loadMeetingDetailsPending(token: String, meetingId: Int, callback:
                                                                MeetingsDataSource.LoadMeetingDetailsPendingCallback) {

        remoteMeetingsDataSource.loadMeetingDetailsPending(token, meetingId, object :
                                                                    MeetingsDataSource.LoadMeetingDetailsPendingCallback {

            override fun onMeetingDetailsLoaded(meetingDetailsPending: MeetingDetailPending) {
                callback.onMeetingDetailsLoaded(meetingDetailsPending)
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }

        })
    }

    override fun deleteMeeting(token: String, meetingId: Int, callback: MeetingsDataSource.DeleteMeetingsCallback) {

        remoteMeetingsDataSource.deleteMeeting(token, meetingId, object : MeetingsDataSource.DeleteMeetingsCallback {
            override fun onMeetingDeleteSuccess() {
                callback.onMeetingDeleteSuccess()
            }

            override fun onMeetingDeleteFail() {
             callback.onMeetingDeleteFail()
            }

        })
    }

    override fun editMeeting(token: String, meeting: EditMeetingRequest) {

        remoteMeetingsDataSource.editMeeting(token, meeting)
    }

    override fun authenticateUser(phoneNumber: Long, otp: Int, callback: MeetingsDataSource.LoadAuthTokenCallback) {

        remoteMeetingsDataSource.authenticateUser(phoneNumber, otp, object : MeetingsDataSource.LoadAuthTokenCallback {
            override fun onAuthTokenLoaded(token: String) {
                callback.onAuthTokenLoaded(token)

            }

            override fun onDataNotAvailable(error: String) {
                callback.onDataNotAvailable("Error in receiving response")
            }

        })

    }

    override fun deleteUser(token: String) {
        remoteMeetingsDataSource.deleteUser(token)
    }


    override fun getOTP(phoneNumber: Long, callback: MeetingsDataSource.LoadOtpCallback) {

        remoteMeetingsDataSource.getOTP(phoneNumber, object : MeetingsDataSource.LoadOtpCallback{
            override fun onOtpReceived(otp: Int) {

            }

            override fun onDataNotAvailable(error: String) {
                callback.onDataNotAvailable(error)
            }

        })
    }
    override fun signUp(token: String, meeting: SignUpRequest, callback: MeetingsDataSource.LoadUserDetailsUpdateCallback) {
            remoteMeetingsDataSource.signUp(token,meeting, object : MeetingsDataSource.LoadUserDetailsUpdateCallback {
                override fun onDetailsLoaded(user: UserUpdateResponse) {
                    callback.onDetailsLoaded(user)
                }

                override fun onDataNotAvailable() {
                    callback.onDataNotAvailable()
                }

            })

    }

}