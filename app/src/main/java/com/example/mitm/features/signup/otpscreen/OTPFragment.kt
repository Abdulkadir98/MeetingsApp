package com.example.mitm.features.signup.otpscreen


import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.mitm.R
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.MeetingsRemoteDataSource
import com.example.mitm.features.signup.SignUpFragmentActivityInteraction
import kotlinx.android.synthetic.main.fragment_ot.*
import org.jetbrains.anko.find
import java.util.*
import java.util.concurrent.TimeUnit


class OTPFragment : Fragment(), OTPContract.View {


    lateinit var timerTextView: TextView

    private lateinit var presenter:OTPPresenter
    private lateinit var callback: SignUpFragmentActivityInteraction

    companion object {
        var PHONE_NUMBRR: String = "PHONE_NUMBER"
    }

    override fun navigateToHomeScreen() {
        callback.navigateToHomeScreen()
    }
    override fun navigateToSignUp() {

        callback.navigateToSignUp()
    }

    fun setOtpEnteredListener(activity: AppCompatActivity) {

        callback = activity as SignUpFragmentActivityInteraction
    }

    override fun showCountDownTimer() {
        object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val text = String.format(Locale.getDefault(), "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60)

                timerTextView.text = text
            }

            override fun onFinish() {
                presenter.onCountdownFinished()
            }
        }.start()
    }

    override fun enableResendOtpBtn() {
        timerTextView.text ="00:00"
    }

    override fun disableResendOtpBtn() {
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_ot, container, false)
        timerTextView = rootView.find(R.id.countdown_timer)
        presenter = OTPPresenter(MeetingsRepository(MeetingsRemoteDataSource(activity?.applicationContext!!)), this, activity?.applicationContext!!)
        presenter.startCountdownTimer()

        return rootView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edt_txt_otp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editable!!.toString().length == 4) {
//                    btn_done.setViewEnabledState()
                    val phoneNumber = arguments?.getLong(PHONE_NUMBRR)
                    presenter.checkIfOtpIsValid(edt_txt_otp.text.toString().toInt(), phoneNumber!!)
                } else {
//                    btn_done.setViewDisabledState()
                }
            }

            override fun beforeTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })


    }

    override fun onResume() {
        super.onResume()
        val actionBar = (activity as AppCompatActivity).getSupportActionBar()
        actionBar?.title = ""
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
    }
}
