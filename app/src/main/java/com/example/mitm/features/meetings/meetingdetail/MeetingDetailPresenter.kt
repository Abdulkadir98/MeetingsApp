package com.example.mitm.features.meetings.meetingdetail

import com.example.mitm.data.models.MeetingDetailPending
import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.requests.InvitationUpdate

class MeetingDetailPresenter(val meetingsRepository: MeetingsRepository, val view: MeetingDetailContract.View) :
                                                                                        MeetingDetailContract.Presenter {
    override fun loadMeetingDetailsPending(token: String, meetingId: Int) {

        meetingsRepository.loadMeetingDetailsPending(token, meetingId, object : MeetingsDataSource.LoadMeetingDetailsPendingCallback{
            override fun onMeetingDetailsLoaded(meetingDetailsPending: MeetingDetailPending) {

                view.showMeetingDetails(meetingDetailsPending)
            }

            override fun onDataNotAvailable() {
            }

        })
    }

    override fun acceptMeeting(token: String, invitationUpdate: InvitationUpdate) {

        meetingsRepository.acceptOrDeclineMeeting(token, invitationUpdate, object : MeetingsDataSource.InvitationUpdateCallback {
            override fun onStatusUpdateSuccess() {
                view.showSucessMessageForStatusUpdate()
            }

            override fun onStatusUpdateFailed() {
                view.showErrorMessageForStatusUpdate()
            }

        })
    }

    override fun declineMeeting(token: String, invitationUpdate: InvitationUpdate) {

        meetingsRepository.acceptOrDeclineMeeting(token, invitationUpdate, object : MeetingsDataSource.InvitationUpdateCallback {
            override fun onStatusUpdateSuccess() {
                view.showSucessMessageForStatusUpdate()
            }

            override fun onStatusUpdateFailed() {
                view.showErrorMessageForStatusUpdate()
            }

        })
    }
}