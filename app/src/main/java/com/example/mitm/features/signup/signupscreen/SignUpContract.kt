package com.example.mitm.features.signup.signupscreen

import android.content.Context
import android.support.v7.app.AppCompatActivity

interface SignUpContract {

    interface View {

        fun checkRuntimePermission()

        fun showPickerDialog()

        fun openGallery()

        fun takepic()

        fun onSuccessText()

        fun onFailedText()

        fun isInternetOn():Boolean

        fun navigateToHomeScreen()

        fun displayLoginErrorMessage()

        fun displayUserImage(url: String)

        fun imageUploadSuccess()

    }

    interface Presenter {

        fun onAddPictureClicked()

        fun onCameraPicked()

        fun onGalleryPicked()

        fun onPermissionGranted()

        fun checkValiedText(fname:String,lname:String)

        fun signUp(token:String)

        fun checkInternetConnection()

        fun checkValidAvatarUrl(url : String)


    }
}