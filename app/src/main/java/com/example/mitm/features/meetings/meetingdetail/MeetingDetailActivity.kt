package com.example.mitm.features.meetings.meetingdetail

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mitm.R
import com.example.mitm.data.models.MeetingDetailPending
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.MeetingsRemoteDataSource
import com.example.mitm.data.source.remote.requests.InvitationUpdate
import com.example.mitm.features.meetings.location.YourLocationActivity
import com.example.mitm.features.meetings.meetingdetail.acceptedmeeting.AcceptMeetingDetailPresenter
import com.example.mitm.features.utils.Constants
import com.example.mitm.features.utils.DelegateExt
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_meeting_detail.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

@Parcelize
data class Contact(val number: Long, val name: String, val status: String, val textColor: Int, val iconUrl: String): Parcelable

class MeetingDetailActivity : AppCompatActivity(), MeetingDetailContract.View {


    private lateinit var presenter: MeetingDetailPresenter
    private var meetingID: Int = -1

    private lateinit var contactsRecyclerView: RecyclerView

    private val accessToken: String by DelegateExt.stringPreference(this
            ,DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)

    companion object {
        val MEETING_ID = "MeetingDetailActivity:meetingId"
    }

    override fun showErrorMessageForStatusUpdate() {
        toast("Some error occurred!")

    }

    override fun showSucessMessageForStatusUpdate() {
        toast("Your status has been notified!")
        finish()

    }
    override fun showMeetingDetails(details: MeetingDetailPending) {

        meetingTitle.text = details.meeting_name
        meetingDateTv.text = details.meeting_start_date
        meetingTimeTv.text = details.meeting_start_time
        meetingDescTv.text = details.meeting_description

        Glide.with(this).load(details.meeting_creator_pic)
                .placeholder(R.mipmap.user_6)
                .circleCrop()
                .into(meetingCreatorIv)

        meetingCreatorTv.text = "Created by ${details.meeting_creator}"

        Log.i("Meeting Details:", details.user_list.toString())

        val contacts = details.user_list.map {
            when(it.status) {
                "pending" -> Contact(it.phone, it.first_name, "Pending", R.color.pending_contact_txt_color, it.avatar_url)
                "rejected"-> Contact(it.phone, it.first_name, "Rejected", R.color.rejected_contact_txt_color, it.avatar_url)
                "accepted" -> Contact(it.phone, it.first_name, "Accepted", R.color.accepted_contact_txt_color, it.avatar_url)
                else -> Contact(0, "", "", 0, "")
            }
        }.toMutableList()

        val contactsAdapter = ContactsAdapter(contacts, this, null)
        contactsRecyclerView.adapter = contactsAdapter


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_detail)
        meetingID = intent.getIntExtra(MEETING_ID, 0)
        title = ""

        val backBtn = findViewById<ImageView>(R.id.back_btn)
        backBtn.setOnClickListener {
            finish()
        }

        contactsRecyclerView = find(R.id.invited_contacts)
        contactsRecyclerView.layoutManager = GridLayoutManager(this, 2)

        presenter = MeetingDetailPresenter(MeetingsRepository(MeetingsRemoteDataSource(this)), this)
        presenter.loadMeetingDetailsPending(accessToken, meetingID)

        declineMeetingBtn.setOnClickListener {

            val invitationUpdate = InvitationUpdate(meeting_id = meetingID, status = "rejected")
            presenter.declineMeeting(accessToken, invitationUpdate)

        }

        acceptMeetingBtn.setOnClickListener {

            startActivityForResult<YourLocationActivity>(Constants.LOCATION_REQUEST_MEETING_PENDING)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == Constants.LOCATION_REQUEST_MEETING_PENDING && resultCode == Activity.RESULT_OK && data != null) {

            val latitude = data.getDoubleExtra(Constants.LATITUTDE, 0.0)
            val longitude = data.getDoubleExtra(Constants.LONGITUDE, 0.0)

            val invitationUpdate = InvitationUpdate(meeting_id = meetingID, latitude = latitude, longitude = longitude,
                    status = "accepted")

            presenter.acceptMeeting(accessToken, invitationUpdate)
        }
    }
}




