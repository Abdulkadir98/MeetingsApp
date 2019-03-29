package com.example.mitm.features.meetings


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.example.mitm.R
import com.example.mitm.data.models.Meeting
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.MeetingsRemoteDataSource
import com.example.mitm.data.source.remote.requests.InvitationUpdate
import com.example.mitm.features.meetings.createmeeting.CreateMeetingActivity
import com.example.mitm.features.meetings.location.YourLocationActivity
import com.example.mitm.features.meetings.meetingdetail.acceptedmeeting.AcceptMeetingDetail
import com.example.mitm.features.meetings.meetingdetail.MeetingDetailActivity

import com.example.mitm.features.userprofile.UserProfileActivity
import com.example.mitm.features.utils.Constants
import com.example.mitm.features.utils.DelegateExt

import kotlinx.android.synthetic.main.activity_my_meetings.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast


class MyMeetingsActivity : AppCompatActivity(), MyMeetingsContract.View {

    private lateinit var recyclerView: RecyclerView
    private lateinit var presenter: MyMeetingsPresenter

    private lateinit var adapter: MyMeetingsAdapter
    private lateinit var progressBar: ProgressBar

    private val accessToken: String by DelegateExt.stringPreference(this
            , DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)

    var avatarUrl: String by DelegateExt.stringPreference(this, Constants.USER_AVATAR_URL, Constants.USER_AVATAR_URL_DEFAULT)


    private var meetingId = 0


    override fun showMeetings(meetings: List<Meeting>) {

        emptyView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        Glide.with(this)
                .load(avatarUrl)
                .error(R.mipmap.user_6)
                .circleCrop()
                .into(userProfileIv)

        adapter.meetings = meetings as MutableList<Meeting>
        adapter.notifyDataSetChanged()

    }

    override fun navigateToYourLocation(id: Int) {
        startActivityForResult<YourLocationActivity>(Constants.LOCATION_REQUEST_YOUR_MEETING)
        meetingId = id
    }


    override fun showEmptyView() {
        emptyView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        retryBtn.setOnClickListener {
            presenter.loadMeetings(accessToken)
        }
    }

    override fun showErrorMessageForStatusUpdate() {
        toast("Some error occurred!")

    }

    override fun showSuccessMessageForStatusUpdate() {
        toast("Your status has been notified!")
    }


    override fun showMeetingDetails(meeting: Meeting) {

        startActivity<MeetingDetailActivity>(MeetingDetailActivity.MEETING_ID to meeting.meeting_id)
    }

    override fun showAcceptedMeetingDetails(meeting: Meeting) {

        startActivity<AcceptMeetingDetail>(AcceptMeetingDetail.MEETING_ID to meeting.meeting_id)
    }

    override fun hideProgressbar() {
        progressBar.visibility = View.GONE
    }

    override fun showProgressbar() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_meetings)

        setSupportActionBar(my_toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        progressBar = find(R.id.progressBar)

        presenter = MyMeetingsPresenter(MeetingsRepository(MeetingsRemoteDataSource(applicationContext)), this)
        presenter.loadMeetings(accessToken)
        recyclerView = find(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        adapter = MyMeetingsAdapter(mutableListOf(), presenter::onListItemClicked, this, presenter::onAcceptMeeting,
                                            presenter::declineMeeting)
        recyclerView.adapter = adapter
        adapter = MyMeetingsAdapter(mutableListOf(), presenter::onListItemClicked, this, presenter::onAcceptMeeting,
                presenter::declineMeeting)
        recyclerView.adapter = adapter

        userProfileIv.setOnClickListener {
            startActivity<UserProfileActivity>()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_meeting_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {

            R.id.create_meeting -> {
                startActivity<CreateMeetingActivity>()
                true
            }

            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onResume() {
        super.onResume()
        presenter.loadMeetings(accessToken)
        Glide.with(this)
                .load(avatarUrl)
                .error(R.mipmap.user_6)
                .circleCrop()
                .into(userProfileIv)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.LOCATION_REQUEST_YOUR_MEETING && resultCode == Activity.RESULT_OK && data != null) {

            val latitude = data.getDoubleExtra(Constants.LATITUTDE, 0.0)
            val longitude = data.getDoubleExtra(Constants.LONGITUDE, 0.0)

            val invitationUpdate = InvitationUpdate(meeting_id = meetingId, latitude = latitude, longitude = longitude,
                                                        status = "accepted")

            presenter.acceptMeeting(accessToken, invitationUpdate)
            presenter.loadMeetings(accessToken)

        }
    }
}
