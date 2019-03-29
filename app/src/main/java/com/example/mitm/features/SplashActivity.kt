package com.example.mitm.features

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.example.mitm.R
import com.example.mitm.features.meetings.MyMeetingsActivity
import com.example.mitm.features.signup.SignUpActivity
import com.example.mitm.features.utils.DelegateExt
import org.jetbrains.anko.startActivity


class SplashActivity : AppCompatActivity() {

    private var accessToken: String by DelegateExt.stringPreference(this,
                                DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({

            if(accessToken.isEmpty())
                startActivity<SignUpActivity>()
            else
                startActivity<MyMeetingsActivity>()
            finish()
        }, 1000)


    }
}
