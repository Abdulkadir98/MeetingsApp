package com.example.mitm.features.signup


import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.mitm.R
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.MeetingsRemoteDataSource
import kotlinx.android.synthetic.main.fragment_phoneauth.*
import org.jetbrains.anko.toast


class PhoneAuthFragment : Fragment(), PhoneAuthContract.View {

    private lateinit var phonenumber: EditText
    private lateinit var termsOfUseTxt: TextView
    private lateinit var nextBtn: TextView
    private lateinit var backBtn: ImageView

    //    private lateinit var menuItem: MenuItem
    private lateinit var presenter: PhoneAuthPresenter
    private lateinit var callback: SignUpFragmentActivityInteraction

    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)


    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_phoneauth, container, false)
        phonenumber = rootView.findViewById(R.id.phonenumber)
        termsOfUseTxt = rootView.findViewById(R.id.termsOfUse)

        nextBtn = rootView.findViewById(R.id.nextButton)
        backBtn = rootView.findViewById(R.id.backButton)

        val policy = Html.fromHtml(resources.getString(R.string.agree_terms_privacy))

        termsOfUseTxt.text = policy
        termsOfUseTxt.movementMethod = LinkMovementMethod.getInstance()

        presenter = PhoneAuthPresenter(MeetingsRepository(MeetingsRemoteDataSource(activity?.applicationContext!!)), this)

        phonenumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {

                presenter.checkIfValidPhoneNumber(phonenumber.text.toString().trim())
            }

        })


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accept_terms_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->

            presenter.checkIfValidPhoneNumber(phonenumber.text.toString().trim())
        }
        nextBtn.setOnClickListener {
            presenter.getOtp(phoneNumber.toLong())
            callback.navigateToOtp(phoneNumber.toLong())
        }
    }

    override fun otpNotReceivedErrorMessage() {
        activity?.toast("Sorry, OTP couldn't be sent!")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.next_menu, menu)
//        menuItem = menu.findItem(R.id.next)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        return (when (item.itemId) {
            R.id.next -> {
                presenter.getOtp(phoneNumber.toLong())
                callback.navigateToOtp(phoneNumber.toLong())
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        })

    }

    override fun onSuccessPhoneAuth() {

        //phonenumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_done_24px, 0);
//        menuItem.isEnabled = true
        phoneNumber = phonenumber.text.toString().trim()
        if (accept_terms_checkbox.isChecked) {
            nextBtn.isEnabled = true
            nextBtn.alpha = 1f
        } else {
            nextBtn.isEnabled = false
            nextBtn.alpha = 0.3f
        }

    }

    override fun onFailedPhoneAuth() {

        phonenumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//        if(::menuItem.isInitialized){
//            menuItem.isEnabled = false
//
//        }

        nextBtn.isEnabled = false
        nextBtn.alpha = 0.3f

    }


    override fun onResume() {
        super.onResume()
        val actionBar = (activity as AppCompatActivity).getSupportActionBar()
        actionBar?.title = ""
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
    }

    fun setOnNextMenuClickedListener(activity: Activity) {

        callback = activity as SignUpFragmentActivityInteraction

    }


}

