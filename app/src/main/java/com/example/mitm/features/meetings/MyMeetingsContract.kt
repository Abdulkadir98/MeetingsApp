package com.example.mitm.features.meetings

import com.example.mitm.data.models.Meeting
import com.example.mitm.data.source.remote.requests.InvitationUpdate

interface MyMeetingsContract {
    interface Presenter {

        fun onListItemClicked(meeting: Meeting)

        fun loadMeetings(token: String)

        fun acceptMeeting(token: String, body: InvitationUpdate)

        fun onAcceptMeeting(meetingId: Int)

        fun declineMeeting(token: String, body: InvitationUpdate)

    }

    interface View {

        fun showMeetings(meetings: List<Meeting>)

        fun showMeetingDetails(meeting: Meeting)

        fun showAcceptedMeetingDetails(meeting: Meeting)

        fun navigateToYourLocation(id: Int)

        fun showSuccessMessageForStatusUpdate()

        fun showErrorMessageForStatusUpdate()

        fun showEmptyView()

        fun showProgressbar()

        fun hideProgressbar()

    }
}