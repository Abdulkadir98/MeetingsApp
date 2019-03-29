package com.example.mitm.features.signup

import android.app.AppComponentFactory
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.Toast
import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.MeetingsRepository


class PhoneAuthPresenter(val meetingsRepository: MeetingsRepository, val phoneAuthView: PhoneAuthContract.View) : PhoneAuthContract.Presenter {

    override fun getOtp(phoneNumber: Long) {

        meetingsRepository.getOTP(phoneNumber, object : MeetingsDataSource.LoadOtpCallback{
            override fun onOtpReceived(otp: Int) {

            }

            override fun onDataNotAvailable(error: String) {

                phoneAuthView.otpNotReceivedErrorMessage()
            }

        })
    }



    override fun checkIfValidPhoneNumber(result: String) =
            if (result.length == 10) {
        phoneAuthView.onSuccessPhoneAuth()


    }
    else{

        phoneAuthView.onFailedPhoneAuth()

    }





}