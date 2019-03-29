package com.example.mitm.features.signup

import android.widget.EditText

interface PhoneAuthContract {
    interface View {

        fun onSuccessPhoneAuth()
        fun onFailedPhoneAuth()

        fun otpNotReceivedErrorMessage()


    }

    interface Presenter {

        fun checkIfValidPhoneNumber(result: String)

        fun getOtp(phoneNumber: Long)

    }
}