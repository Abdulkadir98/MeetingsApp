package com.example.mitm.features.meetings.meetingdetail

import com.example.mitm.data.models.MeetingDetailPending
import com.example.mitm.data.source.remote.requests.InvitationUpdate

interface MeetingDetailContract {

    interface Presenter {

        fun loadMeetingDetailsPending(token: String, meetingId: Int)

        fun acceptMeeting(token: String, invitationUpdate: InvitationUpdate)

        fun declineMeeting(token: String, invitationUpdate: InvitationUpdate)
    }

    interface View {

        fun showMeetingDetails(details: MeetingDetailPending)

        fun showSucessMessageForStatusUpdate()

        fun showErrorMessageForStatusUpdate()
    }
}