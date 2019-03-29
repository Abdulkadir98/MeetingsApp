package com.example.mitm.features.signup

interface SignUpFragmentActivityInteraction {

    fun navigateToOtp(phoneNumber: Long)

    fun navigateToSignUp()

    fun navigateToHomeScreen()
}