package com.example.mitm.features.signup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import com.example.mitm.R
import com.example.mitm.features.meetings.MyMeetingsActivity
import com.example.mitm.features.signup.otpscreen.OTPFragment
import com.example.mitm.features.signup.signupscreen.SignUpFragment
import org.jetbrains.anko.startActivity


class SignUpActivity : AppCompatActivity(), SignUpFragmentActivityInteraction {
    override fun navigateToHomeScreen() {
        startActivity<MyMeetingsActivity>()
        finish()
    }

    override fun navigateToSignUp() {
        val signUpFragment = SignUpFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, signUpFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    override fun navigateToOtp(phoneNumber: Long) {
        val otpFragment = OTPFragment()
        val args = Bundle()
        args.putLong(OTPFragment.PHONE_NUMBRR, phoneNumber)
        otpFragment.arguments = args
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, otpFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


       val fragmentManager = supportFragmentManager
       val fragmentTransaction = fragmentManager.beginTransaction()
       val fragment = PhoneAuthFragment()
        fragmentTransaction.add(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()


    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onAttachFragment(fragment: Fragment) {

        when (fragment) {
            is PhoneAuthFragment -> fragment.setOnNextMenuClickedListener(this)
            is OTPFragment -> fragment.setOtpEnteredListener(this)
            is SignUpFragment -> fragment.setOnNextMenuClickedListener(this)
        }
    }
}
