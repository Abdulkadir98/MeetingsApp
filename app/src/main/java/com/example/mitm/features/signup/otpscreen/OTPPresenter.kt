package com.example.mitm.features.signup.otpscreen

import android.content.Context
import com.example.mitm.data.source.MeetingsDataSource
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.features.utils.Constants
import com.example.mitm.features.utils.DelegateExt


class OTPPresenter(val meetingsRepository: MeetingsRepository, val otpView: OTPContract.View, val context: Context) : OTPContract.Presenter {
    var profileCompleted: Boolean by DelegateExt.booleanPreference(context, Constants.PROFILE_COMPLETED, Constants.PROFILE_COMPLETED_DEFAULT)

    override fun checkIfOtpIsValid(otp: Int, phoneNumber: Long) {

        meetingsRepository.authenticateUser(phoneNumber, otp, object : MeetingsDataSource.LoadAuthTokenCallback{
            override fun onAuthTokenLoaded(token: String) {

                if (profileCompleted)
                otpView.navigateToHomeScreen()
                else
                    otpView.navigateToSignUp()

            }

            override fun onDataNotAvailable(error: String) {

            }

        })
    }

    override fun onCountdownFinished() {
        otpView.enableResendOtpBtn()
    }

    override fun startCountdownTimer() {
        otpView.showCountDownTimer()
    }

    override fun onOtpEntered(otp: Int, phoneNumber: Long) {

        otpView.enableResendOtpBtn()
    }

}