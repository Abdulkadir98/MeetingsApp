package com.example.mitm.features.meetings.meetingdetail.acceptedmeeting

import com.example.mitm.data.models.MeetingDetail

interface AcceptMeetingDetailContract{

    interface View {

        fun showMeetingDetails(meetingDetails: MeetingDetail)

        fun showBottomSheetMenu(status: String)

        fun navigateToCreateMeeting()

        fun displayMeetingInvitationUpdateSuccessMessage(message: String)

        fun displayMeetingInvitationUpdateFailureMessage(message: String)

        fun navigateToHomeScreen()

        fun displayDeleteMeetingErrorMessage()

    }
    interface Presenter {


        fun loadMeetingDetails(token: String, meetingID: Int)

        fun editMeeting()

        fun deleteMeeting(token: String, meetingId: Int)

        fun declineMeeting(token: String, meetingId: Int)


    }



}