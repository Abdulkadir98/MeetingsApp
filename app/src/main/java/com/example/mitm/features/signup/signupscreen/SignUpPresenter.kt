package com.example.mitm.features.signup.signupscreen

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.UserUpdateResponse
import com.example.mitm.data.source.remote.requests.SignUpRequest
import com.example.mitm.data.source.remote.requests.User
import com.example.mitm.features.signup.SignUpActivity


class SignUpPresenter(val meetingsRepository: MeetingsRepository, val signUpView: SignUpContract.View,val context: Context) : SignUpContract.Presenter {


    override fun checkInternetConnection() {

        signUpView.isInternetOn()
    }

    val signUpRequest = SignUpRequest(User("","",""))

    override fun signUp(token: String) {

        meetingsRepository.signUp(token,signUpRequest, object : MeetingsDataSource.LoadUserDetailsUpdateCallback {
            override fun onDetailsLoaded(user: UserUpdateResponse) {
                signUpView.navigateToHomeScreen()
            }

            override fun onDataNotAvailable() {

                signUpView.displayLoginErrorMessage()
            }

        })
    }


    override fun checkValiedText(fname: String,lname:String) {
            if (fname.isNotEmpty() && lname.isNotEmpty()) {
                signUpRequest.user.first_name = fname
                signUpRequest.user.last_name = lname
                signUpView.onSuccessText()

            } else {
                signUpView.onFailedText()
            }

    }

    override fun checkValidAvatarUrl(url: String) {
        if (url.isNotEmpty()){
            signUpRequest.user.avatar_url = url
            signUpView.imageUploadSuccess()
        }
    }

    override fun onPermissionGranted() {
        signUpView.showPickerDialog()

    }

    override fun onCameraPicked() {
        signUpView.takepic()

    }

    override fun onGalleryPicked() {
        signUpView.openGallery()
    }

    override fun onAddPictureClicked() {
        signUpView.checkRuntimePermission()

    }

    fun alreadyHasStoragePermission() {
        signUpView.showPickerDialog()

    }

}