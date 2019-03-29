package com.example.mitm.features.meetings.createmeeting

import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.requests.CreateMeetingRequest
import com.example.mitm.data.source.remote.requests.EditMeetingRequest

class CreateMeetingPresenter(val meetingsRepository: MeetingsRepository, val view: CreateMeetingContract.View) :
        CreateMeetingContract.Presenter {



    val createMeetingRequest = CreateMeetingRequest("", 0, emptyList(), "", "",
            0.0, 0.0)
    private var timeString = ""
    private var dateString = ""

    override fun saveCoordinates(latitude: Double, longitude: Double) {

        createMeetingRequest.latitude = latitude
        createMeetingRequest.longitude = longitude
        view.displayMeetingUpdate()
    }

    override fun saveMeeting(token: String) {
        meetingsRepository.saveMeeting(token, createMeetingRequest, object : MeetingsDataSource.SaveMeetingCallback {
            override fun onMeetingSaved() {
                view.displayMeetingCreatedMessage()
            }

            override fun onDataNotAvailable() {
                view.displayErrorInCreatingMeetingMessage()
            }

        })
    }

    override fun editMeeting(token: String, meetingId: Int) {

        val editMeetingRequest = EditMeetingRequest(meeting_id = meetingId, name = createMeetingRequest.name,
                duration = createMeetingRequest.duration, start_date_time = createMeetingRequest.start_date_time,
                description = createMeetingRequest.description, invited_phone = createMeetingRequest.invited_phone)
        meetingsRepository.editMeeting(token, editMeetingRequest)
    }

    override fun validateMeetingName(name: String) {

        createMeetingRequest.name = name
        validateMeetingDetails()

    }

    override fun validateMeetingDate(date: String) {

        dateString = date
        createMeetingRequest.start_date_time = "$dateString $timeString"
        validateMeetingDetails()

    }

    override fun validateMeetingTime(time: String) {

        timeString = time
        createMeetingRequest.start_date_time = "$dateString $timeString"
        validateMeetingDetails()

    }

    override fun validateMeetingDuration(duration: String) {

        if (duration.isEmpty())
            createMeetingRequest.duration = 0
        else
            createMeetingRequest.duration = duration.toDouble().toInt() * 60
        validateMeetingDetails()

    }

    override fun validateMeetingDescription(desc: String) {

        createMeetingRequest.description = desc
    }

    override fun validateContacts(contacts: List<Long>) {

        createMeetingRequest.invited_phone = contacts
        validateMeetingDetails()
    }

    override fun validateMeetingDetails() {

        if (createMeetingRequest.name.isNotEmpty() && createMeetingRequest.duration > 0 &&
                createMeetingRequest.start_date_time.isNotEmpty() && createMeetingRequest.invited_phone.isNotEmpty()
                && timeString.isNotEmpty() && dateString.isNotEmpty()) {

            view.enableCreateMenuItem()
        } else
            view.disableCreateMenuItem()
    }
}