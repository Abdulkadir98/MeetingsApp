package com.example.mitm.features.userprofile

interface UserProfileContract {

    interface Presenter {

        fun onAddPictureClicked()

        fun onCameraPicked()

        fun onGalleryPicked()

        fun onPermissionGranted()

        fun checkInternetConnection()

        fun checkValidAvatarUrl(url :String)

        fun logout()
    }

    interface View {

        fun checkRuntimePermission()

        fun showPickerDialog()

        fun openGallery()

        fun takePicture()

        fun isInternetOn():Boolean

        fun displayUserImage(url: String)

        fun displayErrorMessageForLoadingImage()

        fun navigateToPhoneAuth()
    }
}