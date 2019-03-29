package com.example.mitm.features.meetings.meetingdetail.acceptedmeeting

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.bumptech.glide.Glide
import com.example.mitm.R
import com.example.mitm.data.models.MeetingDetail
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.MeetingsRemoteDataSource
import com.example.mitm.features.meetings.createmeeting.CreateMeetingActivity
import com.example.mitm.features.meetings.location.FetchAddressIntentService
import com.example.mitm.features.meetings.meetingdetail.Contact
import com.example.mitm.features.meetings.meetingdetail.ContactsAdapter
import com.example.mitm.features.meetings.meetingdetail.PlacesAdapter
import com.example.mitm.features.utils.Constants
import com.example.mitm.features.utils.DelegateExt
import kotlinx.android.synthetic.main.activity_accept_meeting_detail.*
import kotlinx.android.synthetic.main.admin_fragment_bottom_sheet.view.*
import kotlinx.android.synthetic.main.invited_fragment_bottom_sheet.view.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast


data class Place(val name: String, val distance: String, val rating: String, val iconUrl: String)

class AcceptMeetingDetail : AppCompatActivity(), AcceptMeetingDetailContract.View {

    private lateinit var presenter: AcceptMeetingDetailPresenter

    private lateinit var resultReceiver: AddressResultReceiver

    private val accessToken: String by DelegateExt.stringPreference(this
            , DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)

    private var meetingDetails: MeetingDetail? = null

    companion object {
        val MEETING_ID = "AcceptMeetingDetailActivity:meetingId"
    }


    override fun navigateToHomeScreen() {
        toast("Meeting deleted!")
        finish()
    }

    override fun displayDeleteMeetingErrorMessage() {
        toast("Error in deleting Meeting!")
    }

    override fun navigateToCreateMeeting() {

        startActivity<CreateMeetingActivity>()
    }

    override fun displayMeetingInvitationUpdateSuccessMessage(message: String) {
        toast(message)
        finish()
    }

    override fun displayMeetingInvitationUpdateFailureMessage(message: String) {
        toast(message)
    }

    override fun showBottomSheetMenu(status: String) {
        if (status == "admin") {
            val view = layoutInflater.inflate(R.layout.admin_fragment_bottom_sheet, null)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(view)
            dialog.show()

            view.editMeetingTv.setOnClickListener {

                val contacts = meetingDetails?.user_list?.map {

                    // hard coding status to 'remove' because contact in edit meeting screen can be removed
                    Contact(name = it.phone.toString(), status = "Remove", textColor = R.color.rejected_contact_txt_color,
                            iconUrl = it.avatar_url, number = it.phone)
                }?.toMutableList()

                startActivityForResult<CreateMeetingActivity>(
                        Constants.LOCATION_REQUEST_EDIT_MEETING,
                        CreateMeetingActivity.MEETING_ID to meetingDetails?.meeting_id,
                        CreateMeetingActivity.MEETING_NAME to meetingDetails?.meeting_name,
                        CreateMeetingActivity.MEETING_DATE to meetingDetails?.meeting_start_date,
                        CreateMeetingActivity.MEETING_TIME to meetingDetails?.meeting_start_time,
                        CreateMeetingActivity.MEETING_DESC to meetingDetails?.meeting_description,
                        CreateMeetingActivity.MEETING_DURATION to meetingDetails?.duration,
                        CreateMeetingActivity.CONTACTS to contacts)
            }

            view.deleteMeetingTv.setOnClickListener {
                presenter.deleteMeeting(accessToken, meetingDetails?.meeting_id!!)
            }
        } else if (status == "invited") {
            val view = layoutInflater.inflate(R.layout.invited_fragment_bottom_sheet, null)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(view)
            dialog.show()

            view.declineMeetingTv.setOnClickListener {

                presenter.declineMeeting(accessToken, meetingDetails?.meeting_id!!)

            }

            view.reportEventTv.setOnClickListener {

            }
        }

    }

    override fun showMeetingDetails(meetingDetails: MeetingDetail) {

        this.meetingDetails = meetingDetails
        meetingTitle.text = meetingDetails.meeting_name
        meetingDate.text = meetingDetails.meeting_start_date
        meetingTime.text = meetingDetails.meeting_start_time

        meetingDesc.text = meetingDetails.meeting_description

        val location = Location("").apply {
            latitude = meetingDetails.latitude.toDouble()
            longitude = meetingDetails.longitude.toDouble()

        }
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(com.example.mitm.features.meetings.location.Constants.ADDRESS_RECEIVER, resultReceiver)
            putExtra(com.example.mitm.features.meetings.location.Constants.LOCATION_DATA_EXTRA, location)
        }
        startService(intent)

        Glide.with(this)
                .load(meetingDetails.meeting_creator_pic)
                .placeholder(R.mipmap.user_6)
                .circleCrop()
                .into(meetingCreatorIv)

        meetingCreatorTv.text = "Created by ${meetingDetails.meeting_creator}"

        Log.i("Meeting detail users:", meetingDetails.user_list.toString())
        val contacts = meetingDetails.user_list.map {
            when (it.status) {
                0 -> Contact(it.phone, it.first_name, "Pending", R.color.pending_contact_txt_color, it.avatar_url)
                1 -> Contact(it.phone, it.first_name, "Rejected", R.color.rejected_contact_txt_color, it.avatar_url)
                2 -> Contact(it.phone, it.first_name, "Accepted", R.color.accepted_contact_txt_color, it.avatar_url)
                else -> Contact(0, "", "", 0, "")
            }
        }.toMutableList()

        val contactsAdapter = ContactsAdapter(contacts, this, null)
        invitedContacts.adapter = contactsAdapter


        val places = meetingDetails.places.map {

            val rating = it.rating ?: "No"
            Place(it.name, "${it.distance_in_kms} km away", "$rating Rating", it.icon)

        }

        val adapter = PlacesAdapter(places, this)
        suggestedPlaces.adapter = adapter


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_meeting_detail)

        val meetingId = intent.getIntExtra(MEETING_ID, -1)

        presenter = AcceptMeetingDetailPresenter(MeetingsRepository(MeetingsRemoteDataSource(this)), this)
        presenter.loadMeetingDetails(accessToken, meetingId)

        resultReceiver = AddressResultReceiver(Handler())

        invitedContacts.layoutManager = GridLayoutManager(this, 2)
        invitedContacts.adapter = ContactsAdapter(mutableListOf(), this, null)

        suggestedPlaces.layoutManager = LinearLayoutManager(this)
        suggestedPlaces.adapter = PlacesAdapter(emptyList(), this)


        optionsMenu.setOnClickListener {

            showBottomSheetMenu(meetingDetails?.status!!)

        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    private var addressOutput: String = ""

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {

            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData?.getString(com.example.mitm.features.meetings.location.Constants.ADDRESS_RESULT_DATA_KEY) ?: ""

            val addresses = addressOutput.split(',')
            val address = addresses[addresses.size - 2] + addresses[addresses.size - 1]

            // Show a toast message if an address was found.
            if (resultCode == com.example.mitm.features.meetings.location.Constants.SUCCESS_RESULT) {
                displayAddressOutput(addressOutput)
            }

        }
    }

    fun displayAddressOutput(address: String) {
        meetingAddr.text = address
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == Constants.LOCATION_REQUEST_EDIT_MEETING && resultCode == Activity.RESULT_OK && data != null) {
            finish()
        }
    }
}
