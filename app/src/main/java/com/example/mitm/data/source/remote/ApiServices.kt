package com.example.mitm.data.source.remote

import com.example.mitm.data.models.MeetingDetail
import com.example.mitm.data.models.MeetingDetailPending
import com.example.mitm.data.source.remote.requests.*

import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiServices {

    @POST("auth/login")
    fun getOTP(@Body body: MeetingsRemoteDataSource.ContactRequestBody): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("auth/verify_otp")
    fun authenticateUser(@Body body: MeetingsRemoteDataSource.VerifyOtpRequestBody): Call<AuthResponse>

    @PUT("invitations/update")
    fun acceptOrDeclineMeeting(@Body body: InvitationUpdate, @Header("Access-Token") token: String) : Call<ResponseBody>

    @POST("meetings")
    fun saveMeeting(@Body body: CreateMeetingRequest, @Header("Access-Token") token: String) : Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("meetings")
    fun loadMeetings(@Header("Access-Token")token: String) : Call<MeetingsResponse>

    @Headers("Accept: application/json")
    @PUT("users/update")
    fun signUp(@Body body: SignUpRequest, @Header("Access-Token")token: String ): Call<UserUpdateResponse>

    @Headers("Accept: application/json")
    @POST("meetings/meetings_details")
    fun loadMeetingDetails(@Body body: MeetingDetailRequest, @Header("Access-Token")token: String): Call<MeetingDetail>

    @Headers("Accept: application/json")
    @POST("meetings/meetings_details_pending")
    fun loadMeetingDetailsPending(@Body body: MeetingDetailRequest, @Header("Access-Token")token: String):
                                    Call<MeetingDetailPending>

    @Headers("Accept: application/json")
    @PUT("meetings/update")
    fun editMeeting(@Body body: EditMeetingRequest, @Header("Access-Token")token: String): Call<ResponseBody>

    @HTTP(method = "DELETE", path = "meetings/delete_meeting", hasBody = true)
    fun deleteMeeting(@Body body: MeetingDetailRequest, @Header("Access-Token")token: String) : Call<ResponseBody>

    @DELETE("users/signout")
    fun deleteUser(@Header("Access-Token")token: String) : Call<ResponseBody>

}

object RetrofitClient {
    private var retrofit: Retrofit? = null


    fun getClient(baseUrl: String): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(StethoInterceptor())
            .build()
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}