package com.example.mitm.features.meetings

import android.util.Log
import com.example.mitm.data.models.Meeting
import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.requests.InvitationUpdate

class MyMeetingsPresenter(val meetingsRepository: MeetingsRepository, val meetingsView: MyMeetingsContract.View): MyMeetingsContract.Presenter {


    override fun acceptMeeting(token: String, body: InvitationUpdate) {
        meetingsRepository.acceptOrDeclineMeeting(token, body, object : MeetingsDataSource.InvitationUpdateCallback {
            override fun onStatusUpdateSuccess() {
                meetingsView.showSuccessMessageForStatusUpdate()
            }

            override fun onStatusUpdateFailed() {
                meetingsView.showErrorMessageForStatusUpdate()
            }

        })
    }

    override fun onAcceptMeeting(meetingId: Int) {
        meetingsView.navigateToYourLocation(meetingId)
    }

    override fun declineMeeting(token: String, body: InvitationUpdate) {
        meetingsRepository.acceptOrDeclineMeeting(token, body, object : MeetingsDataSource.InvitationUpdateCallback {
            override fun onStatusUpdateSuccess() {
                meetingsView.showSuccessMessageForStatusUpdate()
            }

            override fun onStatusUpdateFailed() {
                meetingsView.showErrorMessageForStatusUpdate()
            }

        })

    }

    override fun loadMeetings(token: String) {

        meetingsView.showProgressbar()
        meetingsRepository.loadMeetings(token, object : MeetingsDataSource.LoadMeetingsCallback{
            override fun onMeetingsLoaded(meetings: List<Meeting>) {

                meetingsView.hideProgressbar()
                Log.i("Meeting Presenter:", meetings.isEmpty().toString())
                if (meetings.isEmpty())
                    meetingsView.showEmptyView()
                else {
                    meetingsView.showMeetings(meetings)
                }
            }

            override fun onDataNotAvailable(error: String) {
                meetingsView.showEmptyView()
                meetingsView.hideProgressbar()
            }

        })
    }

    override fun onListItemClicked(meeting: Meeting) {

        if (meeting.meeting_status == "admin" || meeting.meeting_status == "accepted")
            meetingsView.showAcceptedMeetingDetails(meeting)
        else
            meetingsView.showMeetingDetails(meeting)

    }
}