package com.example.mitm.features.signup.otpscreen

interface OTPContract {
    interface View {

        fun showCountDownTimer()

        fun enableResendOtpBtn()

        fun disableResendOtpBtn()

        fun navigateToSignUp()

        fun navigateToHomeScreen()
    }

    interface Presenter {

        fun onOtpEntered(otp: Int, phoneNumber: Long)

        fun startCountdownTimer()

        fun onCountdownFinished()

        fun checkIfOtpIsValid(otp: Int, phoneNumber: Long)
    }
}

