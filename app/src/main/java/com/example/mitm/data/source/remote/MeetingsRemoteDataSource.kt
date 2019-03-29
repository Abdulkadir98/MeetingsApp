package com.example.mitm.data.source.remote

import android.content.Context
import android.util.Log
import com.example.mitm.data.models.MeetingDetail
import com.example.mitm.data.models.MeetingDetailPending
import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.remote.requests.*
import com.example.mitm.features.utils.Constants

import com.example.mitm.features.utils.DelegateExt
import okhttp3.ResponseBody
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MeetingsRemoteDataSource(val context: Context): MeetingsDataSource {



    var accessToken: String by DelegateExt.stringPreference(context, DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)
    var userId: Int by DelegateExt.intPreference(context, Constants.USER_ID, Constants.USER_ID_DEFAULT)
    var userFirstName: String by DelegateExt.stringPreference(context, Constants.USER_FIRST_NAME, Constants.USER_FIRST_NAME_DEFAULT)
    var userLastName: String by DelegateExt.stringPreference(context, Constants.USER_LAST_NAME, Constants.USER_LAST_NAME_DEFAULT)
    var avatarUrl: String by DelegateExt.stringPreference(context, Constants.USER_AVATAR_URL, Constants.USER_AVATAR_URL_DEFAULT)
    var profileCompleted: Boolean by DelegateExt.booleanPreference(context, Constants.PROFILE_COMPLETED, Constants.PROFILE_COMPLETED_DEFAULT)

    val apiServices:ApiServices = RetrofitClient
            .getClient("http://production.p7cpcpp8yc.us-east-1.elasticbeanstalk.com/api/v1/")
            .create(ApiServices::class.java)

    override fun acceptOrDeclineMeeting(token: String, body: InvitationUpdate, callback: MeetingsDataSource.InvitationUpdateCallback) {

        val call: Call<ResponseBody> = apiServices.acceptOrDeclineMeeting(body, token)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
              callback.onStatusUpdateFailed()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    callback.onStatusUpdateSuccess()

                }
            }

        })
    }


    override fun saveMeeting(token: String, meeting: CreateMeetingRequest, callback: MeetingsDataSource.SaveMeetingCallback) {

        val call: Call<ResponseBody> = apiServices.saveMeeting(meeting, token)
        Log.i("create meeting", token)
        call.enqueue(object : Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback.onDataNotAvailable()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {

                    Log.i("create meeting", response.body().toString())
                    callback.onMeetingSaved()
                }
            }

        })
    }

    override fun loadMeetingDetails(token: String, meetingId: Int, callback: MeetingsDataSource.LoadMeetingDetailsCallback) {

        val meetingDetailRequest = MeetingDetailRequest(meetingId)
        val call: Call<MeetingDetail> = apiServices.loadMeetingDetails(meetingDetailRequest, token)

        call.enqueue(object : Callback<MeetingDetail> {
            override fun onResponse(call: Call<MeetingDetail>, response: Response<MeetingDetail>) {

                if (response.code() == 200){

                    val meetingDetails = response.body()
                    callback.onMeetingDetailsLoaded(meetingDetails!!)

                    Log.i("Meeting details:", meetingDetails.meeting_creator)

                }
            }

            override fun onFailure(call: Call<MeetingDetail>, t: Throwable) {
                callback.onDataNotAvailable()
            }

        })
    }

    override fun loadMeetingDetailsPending(token: String, meetingId: Int, callback:
                                                                MeetingsDataSource.LoadMeetingDetailsPendingCallback) {

        val meetingDetailRequest = MeetingDetailRequest(meetingId)
        val call: Call<MeetingDetailPending> = apiServices.loadMeetingDetailsPending(meetingDetailRequest, token)

        call.enqueue(object : Callback<MeetingDetailPending>{
            override fun onFailure(call: Call<MeetingDetailPending>, t: Throwable) {
                callback.onDataNotAvailable()
            }

            override fun onResponse(call: Call<MeetingDetailPending>, response: Response<MeetingDetailPending>) {

                if (response.code() == 200) {

                    val meetingDetailPending = response.body()
                    callback.onMeetingDetailsLoaded(meetingDetailPending!!)

                    Log.i("Pending meeting", meetingDetailPending.meeting_creator)
                }
            }

        })
    }

    override fun deleteMeeting(token: String, meetingId: Int, callback: MeetingsDataSource.DeleteMeetingsCallback) {

        val deleteMeetingRequest = MeetingDetailRequest(meetingId)
        val call: Call<ResponseBody> = apiServices.deleteMeeting(deleteMeetingRequest, token)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback.onMeetingDeleteFail()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.code() == 200) {
                    callback.onMeetingDeleteSuccess()
                }
            }

        })
    }

    override fun editMeeting(token: String, meeting: EditMeetingRequest) {

        val call: Call<ResponseBody> = apiServices.editMeeting(meeting, token)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    context.toast("Meeting updated successfully!")
                }
            }

        })
    }

    override fun loadMeetings(token: String, callback: MeetingsDataSource.LoadMeetingsCallback) {

       val call: Call<MeetingsResponse> = apiServices.loadMeetings(token)

        call.enqueue(object : Callback<MeetingsResponse> {
            override fun onFailure(call: Call<MeetingsResponse>, t: Throwable) {
                Log.e("Your meetings", "Error in fetching response")
                callback.onDataNotAvailable("error in fetching response")
            }

            override fun onResponse(call: Call<MeetingsResponse>, response: Response<MeetingsResponse>) {
                if (response.code() == 200) {

                    val meetings = response.body()?.my_meetings
                    if (userFirstName == "")
                    userFirstName = response.body()?.first_name!!
                    if (userLastName == "")
                    userLastName = response.body()?.last_name!!
                    if (avatarUrl == "")
                    avatarUrl = response.body()?.avatar_url!!
                    callback.onMeetingsLoaded(meetings!!)

                    Log.i("Your meetings", meetings.toString())
                }
            }

        })
    }

    data class VerifyOtpRequestBody(val otp: Int, val phone: Long)
    override fun authenticateUser(phoneNumber: Long, otp: Int, callback: MeetingsDataSource.LoadAuthTokenCallback) {

        val call: Call<AuthResponse> = apiServices.authenticateUser(VerifyOtpRequestBody(otp, phoneNumber))

        call.enqueue(object :Callback<AuthResponse> {
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                callback.onDataNotAvailable("Error in getting response")
            }

            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {

                if (response.code() == 200) {
                    val authResponse = response.body()
                    accessToken = authResponse?.token!!
                    userId = authResponse?.id!!
                    profileCompleted = authResponse?.profile_completed


                    Log.i("Verify OTP", authResponse?.token)
                    Log.i("Verify OTP", authResponse?.id.toString())
                    callback.onAuthTokenLoaded(authResponse!!.token)
                }

            }

        })
    }

    data class ContactRequestBody(val phone: Long)

    override fun getOTP(phoneNumber: Long, callback: MeetingsDataSource.LoadOtpCallback) {

        val call: Call<ResponseBody> = apiServices.getOTP(ContactRequestBody(phoneNumber))
        call.enqueue(object: Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback.onDataNotAvailable(" Error in fetching data")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){

                }
            }

        })
    }

    override fun deleteUser(token: String) {

        val call: Call<ResponseBody> = apiServices.deleteUser(token)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                context.toast("Error logging out!")
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    context.toast("Log out successful!")
                }
            }

        })
    }

    override fun signUp(token: String, meeting: SignUpRequest, callback: MeetingsDataSource.LoadUserDetailsUpdateCallback) {
        val call: Call<UserUpdateResponse> = apiServices.signUp(meeting,token)
        call.enqueue(object : Callback<UserUpdateResponse> {
            override fun onFailure(call: Call<UserUpdateResponse>, t: Throwable) {
                callback.onDataNotAvailable()
            }

            override fun onResponse(call: Call<UserUpdateResponse>, response: Response<UserUpdateResponse>) {
                if (response.code() == 200) {

                    val user = response.body()
                    userFirstName = user?.first_name!!
                    userLastName = user?.last_name!!
                    avatarUrl = user?.avatar_url!!

                    callback.onDetailsLoaded(user)
                    Log.i("create new sign up", user.toString())
                }
            }

        })
    }


}