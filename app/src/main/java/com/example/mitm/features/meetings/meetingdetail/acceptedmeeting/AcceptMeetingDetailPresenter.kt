package com.example.mitm.features.meetings.meetingdetail.acceptedmeeting

import com.example.mitm.data.models.MeetingDetail
import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.requests.InvitationUpdate

class AcceptMeetingDetailPresenter(val meetingsRepository: MeetingsRepository, val acceptMeetingDetailView: AcceptMeetingDetailContract.View) : AcceptMeetingDetailContract.Presenter {

    override fun deleteMeeting(token: String, meetingId: Int) {

        meetingsRepository.deleteMeeting(token, meetingId, object : MeetingsDataSource.DeleteMeetingsCallback {
            override fun onMeetingDeleteSuccess() {
                acceptMeetingDetailView.navigateToHomeScreen()
            }

            override fun onMeetingDeleteFail() {
             acceptMeetingDetailView.displayDeleteMeetingErrorMessage()
            }

        })
        acceptMeetingDetailView.navigateToHomeScreen()
    }

    override fun editMeeting() {

        acceptMeetingDetailView.navigateToCreateMeeting()
    }

    override fun declineMeeting(token: String, meetingId: Int) {

        val invitationUpdate = InvitationUpdate(meeting_id = meetingId, status = "rejected")
        meetingsRepository.acceptOrDeclineMeeting(token, invitationUpdate, object : MeetingsDataSource.InvitationUpdateCallback {
            override fun onStatusUpdateSuccess() {
                acceptMeetingDetailView.displayMeetingInvitationUpdateSuccessMessage("Status has been notified!")
            }

            override fun onStatusUpdateFailed() {
                acceptMeetingDetailView.displayMeetingInvitationUpdateFailureMessage("Error occurred")
            }

        })
    }

    override fun loadMeetingDetails(token: String, meetingID: Int) {

        meetingsRepository.loadMeetingDetails(token, meetingID, object: MeetingsDataSource.LoadMeetingDetailsCallback {
            override fun onMeetingDetailsLoaded(meetingDetails: MeetingDetail) {
                acceptMeetingDetailView.showMeetingDetails(meetingDetails)
            }

            override fun onDataNotAvailable() {
                
            }

        })
    }

}