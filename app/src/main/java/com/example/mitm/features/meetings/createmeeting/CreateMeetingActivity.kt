package com.example.mitm.features.meetings.createmeeting

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import com.example.mitm.R
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.MeetingsRemoteDataSource
import com.example.mitm.features.meetings.location.YourLocationActivity
import com.example.mitm.features.meetings.meetingdetail.Contact
import com.example.mitm.features.meetings.meetingdetail.ContactsAdapter
import com.example.mitm.features.meetings.meetingdetail.OnContactRemovedListener
import com.example.mitm.features.utils.Constants
import com.example.mitm.features.utils.DelegateExt
import com.example.mitm.features.utils.getLastTenDigits
import kotlinx.android.synthetic.main.activity_create_meeting.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast


class CreateMeetingActivity : AppCompatActivity(), DateAndTimeFragmentActivityInteraction, CreateMeetingContract.View,
        OnContactRemovedListener {

    private lateinit var mMeetingName: EditText
    private lateinit var mMeetingDate: EditText
    private lateinit var mMeetingTime: EditText
    private lateinit var mMeetingDuration: EditText
    private lateinit var mMeetingDesc: EditText
    private lateinit var mInviteContacts: Button

    private lateinit var menuItem: MenuItem
    private lateinit var invitedContactsRecyclerView: RecyclerView

    private lateinit var meetingNameTextWatcher: TextWatcher
    private lateinit var meetingDurationTextWatcher: TextWatcher
    private lateinit var meetingDescTextWatcher: TextWatcher


    private lateinit var timePickerFragment: TimePickerFragment
    private lateinit var datePickerFragment: DatePickerFragment

    private lateinit var presenter: CreateMeetingPresenter

    private var contacts = mutableListOf<Contact>()
    private var contactsList = mutableListOf<Long>()
    private val accessToken: String by DelegateExt.stringPreference(this,
            DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)

    private var contactsAdapter = ContactsAdapter(contacts, this, this)
    private val SELECT_PHONE_NUMBER: Int = 1

    private var isEdit = false


    companion object {
        val MEETING_NAME = "CreateMeetingActivity:name"
        val MEETING_DATE = "CreateMeetingActivity:date"
        val MEETING_TIME = "CreateMeetingActivity:time"
        val MEETING_DURATION = "CreateMeetingActivity:duration"
        val MEETING_DESC = "CreateMeetingActivity:desc"
        val MEETING_ID = "CreateMeetingActivity:meetingId"
        val CONTACTS = "CreateMeetingActivity:contacts"
    }


    override fun enableCreateMenuItem() {

        if (::menuItem.isInitialized) {
            menuItem.isEnabled = true
        }
    }

    override fun disableCreateMenuItem() {

        if (::menuItem.isInitialized) {
            menuItem.isEnabled = false
        }
    }


    override fun onTimeSelected(time: String) {
        mMeetingTime.setText(time)
        presenter.validateMeetingTime(time)
    }

    override fun onDateSelected(date: String, default: String) {

        mMeetingDate.setText(date)
        presenter.validateMeetingDate(default)
    }

    override fun onContactRemoved(contact: Contact) {
        contactsList.remove(contact.number)
        presenter.validateContacts(contactsList)
    }

    override fun displayMeetingUpdate() {
        presenter.saveMeeting(accessToken)

    }

    override fun displayErrorInCreatingMeetingMessage() {
        toast("Error in creating meeting!")
    }

    override fun displayMeetingCreatedMessage() {
        toast("Meeting created!")
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_meeting)

        presenter = CreateMeetingPresenter(MeetingsRepository(MeetingsRemoteDataSource(applicationContext)), this)
        meetingNameTextWatcher = MeetingNameTextWatcher()
        meetingDurationTextWatcher = MeetingDurationTextWatcher()
        meetingDescTextWatcher = MeetingDescTextWatcher()

        initViews()
        if (intent.getStringExtra(MEETING_NAME) != null) {
            populateViewsIfIntentNotEmpty()

        }

        timePickerFragment = TimePickerFragment()
        datePickerFragment = DatePickerFragment()


        invitedContactsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        invitedContactsRecyclerView.adapter = contactsAdapter

        mMeetingDate.setOnClickListener {

            datePickerFragment.onDateSelectedListener(this)
            datePickerFragment.show(supportFragmentManager, "datePicker")
        }

        mMeetingTime.setOnClickListener {

            timePickerFragment.setOnTimeSelectedListener(this)
            timePickerFragment.show(supportFragmentManager, "timePicker")
        }
        mInviteContacts.setOnClickListener {

            val contactPickerIntent = Intent(Intent.ACTION_PICK)
            contactPickerIntent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            startActivityForResult(contactPickerIntent, SELECT_PHONE_NUMBER)
        }
    }

    private fun initViews() {
        mMeetingName = find(R.id.meeting_name)
        mMeetingDate = find(R.id.date)
        mMeetingTime = find(R.id.time)
        mMeetingDuration = find(R.id.duration)
        mMeetingDesc = find(R.id.short_desc)

        mInviteContacts = find(R.id.invite_contacts)
        invitedContactsRecyclerView = find(R.id.invitedContacts)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""

        mMeetingName.addTextChangedListener(meetingNameTextWatcher)
        mMeetingDuration.addTextChangedListener(meetingDurationTextWatcher)
        mMeetingDesc.addTextChangedListener(meetingDescTextWatcher)


    }

    // Method called if coming to edit meeting
    fun populateViewsIfIntentNotEmpty() {

        isEdit = true

        mMeetingName.removeTextChangedListener(meetingNameTextWatcher)
        val meetingName = intent.getStringExtra(MEETING_NAME)
        presenter.validateMeetingName(meetingName)
        mMeetingName.setText(meetingName)
        mMeetingName.addTextChangedListener(meetingNameTextWatcher)


        mMeetingDuration.removeTextChangedListener(meetingDurationTextWatcher)
        val duration = intent.getDoubleExtra(MEETING_DURATION, 0.0).toString()
        presenter.validateMeetingDuration(duration)
        mMeetingDuration.setText(duration)
        mMeetingDuration.addTextChangedListener(meetingDurationTextWatcher)

        mMeetingDesc.removeTextChangedListener(meetingDescTextWatcher)
        mMeetingDesc.setText(intent.getStringExtra(MEETING_DESC))
        mMeetingDesc.addTextChangedListener(meetingDescTextWatcher)

        val time = intent.getStringExtra(MEETING_TIME)
        onTimeSelected(time)

        val date = intent.getStringExtra(MEETING_DATE)
        onDateSelected(date, date)

        contacts = intent.getParcelableArrayListExtra(CONTACTS)
        contactsList = contacts.map { it.number }.toMutableList()
        presenter.validateContacts(contactsList)
        contactsAdapter = ContactsAdapter(contacts, this, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.created_meeting_menu, menu)
        menuItem = menu.findItem(R.id.created_meeting)
        if (isEdit) {
            menuItem.title = "SAVE"
            menuItem.isEnabled = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.created_meeting -> {
                if (isEdit) {
                    presenter.editMeeting(accessToken, intent.getIntExtra(MEETING_ID, -1))
                    finish()

                } else {
                    startActivityForResult<YourLocationActivity>(Constants.LOCATION_REQUEST_CREATE_MEETING)
                }
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == SELECT_PHONE_NUMBER && resultCode == Activity.RESULT_OK) {

            val contactUri = data?.data

            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

            val cursor = this.contentResolver.query(contactUri, projection, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val number = cursor.getString(numberIndex).toString().replace(" ", "")

                val phoneNumber = number.getLastTenDigits().toLong()
                contactsList.add(phoneNumber)
                presenter.validateContacts(contactsList)

                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val name = cursor.getString(nameIndex)

                cursor.close()

                contactsAdapter.addContact(Contact(name = name, status = "Remove",
                        textColor = R.color.rejected_contact_txt_color, number = phoneNumber, iconUrl = ""))

            }
        }

        if (requestCode == Constants.LOCATION_REQUEST_CREATE_MEETING && resultCode == Activity.RESULT_OK && data != null) {

            val latitude = data.getDoubleExtra(Constants.LATITUTDE, 0.0)
            val longitude = data.getDoubleExtra(Constants.LONGITUDE, 0.0)
            presenter.saveCoordinates(latitude, longitude)
            Log.i("Create Meeting Coord:", "$latitude, $longitude")
        }

    }

    inner class MeetingNameTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter.validateMeetingName(s.toString().trim())

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            presenter.validateMeetingName(s.toString().trim())

            if (s.isNullOrEmpty()) {
                nameInputLayout.error = "Can't leave this field empty"
                mMeetingName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_text_incomplete, 0)
            } else {
                nameInputLayout.error = null
                mMeetingName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }

    }

    inner class MeetingDurationTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            presenter.validateMeetingDuration(s.toString().trim())

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            presenter.validateMeetingDuration(s.toString().trim())

            if (s.isNullOrEmpty()) {
                durationInputLayout.error = "Can't leave this field empty"
                mMeetingDuration.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_text_incomplete, 0)
            } else {
                durationInputLayout.error = null
                mMeetingDuration.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

            }
        }

    }

    inner class MeetingDescTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            presenter.validateMeetingDescription(s.toString().trim())

        }

    }
}

interface DateAndTimeFragmentActivityInteraction {

    fun onTimeSelected(time: String)

    fun onDateSelected(date: String, default: String)
}
