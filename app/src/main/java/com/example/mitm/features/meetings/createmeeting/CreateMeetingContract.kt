package com.example.mitm.features.meetings.createmeeting


interface CreateMeetingContract {

    interface Presenter {

        fun validateMeetingDetails()

        fun validateMeetingName(name: String)

        fun validateMeetingDate(date: String)

        fun validateMeetingTime(time: String)

        fun validateMeetingDuration(duration: String)

        fun validateMeetingDescription(desc: String)

        fun validateContacts(contacts: List<Long>)

        fun saveMeeting(token: String)

        fun editMeeting(token: String, meetingId: Int)

        fun saveCoordinates(latitude: Double, longitude: Double)
    }

    interface View {

        fun enableCreateMenuItem()

        fun disableCreateMenuItem()

        fun displayMeetingUpdate()

        fun displayMeetingCreatedMessage()

        fun displayErrorInCreatingMeetingMessage()
    }
}