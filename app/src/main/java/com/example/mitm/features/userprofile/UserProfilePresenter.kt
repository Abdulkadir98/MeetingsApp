package com.example.mitm.features.userprofile

import android.content.Context
import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.UserUpdateResponse
import com.example.mitm.data.source.remote.requests.SignUpRequest
import com.example.mitm.data.source.remote.requests.User
import com.example.mitm.features.utils.Constants
import com.example.mitm.features.utils.DelegateExt

class UserProfilePresenter(val meetingsRepository: MeetingsRepository, val view: UserProfileContract.View, val context: Context): UserProfileContract.Presenter {

    var userFirstName: String by DelegateExt.stringPreference(context, Constants.USER_FIRST_NAME, Constants.USER_FIRST_NAME_DEFAULT)
    var userLastName: String by DelegateExt.stringPreference(context, Constants.USER_LAST_NAME, Constants.USER_LAST_NAME_DEFAULT)
    var avatarUrl: String by DelegateExt.stringPreference(context, Constants.USER_AVATAR_URL, Constants.USER_AVATAR_URL_DEFAULT)
    var accessToken: String by DelegateExt.stringPreference(context, DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)

    val signUpRequest = SignUpRequest(User(avatarUrl,userFirstName,userLastName))

    override fun checkValidAvatarUrl(url: String) {
        view.displayUserImage(url)
        signUpRequest.user.avatar_url = url

        meetingsRepository.signUp(accessToken, signUpRequest, object : MeetingsDataSource.LoadUserDetailsUpdateCallback {
            override fun onDetailsLoaded(user: UserUpdateResponse) {
                view.displayUserImage(user.avatar_url)
            }

            override fun onDataNotAvailable() {
                view.displayErrorMessageForLoadingImage()
            }

        })
    }

    override fun logout() {
        meetingsRepository.deleteUser(accessToken)
        accessToken = ""
        userFirstName = ""
        userLastName = ""
        avatarUrl = ""
        view.navigateToPhoneAuth()
    }

    override fun onAddPictureClicked() {
        view.checkRuntimePermission()

    }

    override fun onCameraPicked() {
        view.takePicture()

    }

    override fun onGalleryPicked() {
        view.openGallery()

    }

    override fun onPermissionGranted() {
        view.showPickerDialog()

    }

    override fun checkInternetConnection() {
        view.isInternetOn()

    }

    fun alreadyHasStoragePermission() {
        view.showPickerDialog()

    }
}