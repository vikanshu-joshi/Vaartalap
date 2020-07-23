package com.vikanshu.vaartalap.LoginActivity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.hbb20.CountryCodePicker
import com.vikanshu.vaartalap.HomeActivity.HomeActivity
import com.vikanshu.vaartalap.R
import com.vikanshu.vaartalap.UserDetails.UserDetailsActivity
import dmax.dialog.SpotsDialog
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var countryCode: CountryCodePicker
    private lateinit var userPhone: EditText
    private lateinit var sendOTP: Button
    private lateinit var verifyOTP: Button
    private lateinit var userOTPRequest: TextView
    private lateinit var userOTP: EditText
    private lateinit var otpLayout: LinearLayout
    private lateinit var numberLayout: LinearLayout
    private lateinit var changeNumber: TextView
    private var COUNTRY_CODE = "+91"
    private var NUMBER = ""
    private var otpWaiting = false
    private lateinit var auth: FirebaseAuth
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var storedVerificationId: String = ""
    private lateinit var firestore: FirebaseFirestore

    private lateinit var progressDialog: AlertDialog

    private var otpEntered = ""

    private val OTP_WAITING_STATE = "otpwaiting"
    private val NUMBER_STATE = "number"
    private val CODE_STATE = "code"
    private val VERIFICATION_ID_STATE = "verificationId"
    private val OTP_STATE = "otp"


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        countryCode = findViewById(R.id.countryCode)
        userPhone = findViewById(R.id.userPhoneLogin)
        sendOTP = findViewById(R.id.otp_login)
        userOTP = findViewById(R.id.userOTP)
        userOTPRequest = findViewById(R.id._userPhoneShow)
        otpLayout = findViewById(R.id.otpEnterLayout)
        numberLayout = findViewById(R.id.number_input_layout)
        verifyOTP = findViewById(R.id.verifyOTP)
        changeNumber = findViewById(R.id.change_number)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        progressDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Please Wait ..........")
            .setCancelable(false)
            .build()
        countryCode.setOnCountryChangeListener {
            COUNTRY_CODE = countryCode.selectedCountryCodeWithPlus
        }

        sendOTP.setOnClickListener {
            val number = userPhone.text.toString()
            if (number.isEmpty())
                showToast("ENTER A VALID MOBILE NUMBER")
            else {
                progressDialog.show()
                NUMBER = number
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    COUNTRY_CODE + NUMBER,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            if (progressDialog.isShowing)
                                progressDialog.dismiss()
                            userOTP.setText(credential.smsCode)
                            signInWithPhoneAuthCredential(credential)
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            if (progressDialog.isShowing)
                                progressDialog.dismiss()
                            when (e) {
                                is FirebaseAuthInvalidCredentialsException -> {
                                    showToast("INVALID NUMBER")
                                }
                                is FirebaseTooManyRequestsException -> {
                                    showToast("TOO MANY REQUESTS, TRY AGAIN LATER")
                                }
                                is FirebaseNetworkException -> {
                                    showToast("NO INTERNET")
                                }
                                else -> {
                                    showToast("SOME ERROR OCCURRED")
                                }
                            }
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            if (progressDialog.isShowing)
                                progressDialog.dismiss()
                            showToast("CODE SENT")

                            storedVerificationId = verificationId
                            resendToken = token

                            numberLayout.visibility = View.GONE
                            otpLayout.visibility = View.VISIBLE
                            otpLayout.requestFocus()
                            userOTPRequest.text =
                                "Please enter the verification code sent to ${"$COUNTRY_CODE-$NUMBER"}"
                            otpWaiting = true
                        }
                    })

            }
        }

        verifyOTP.setOnClickListener {
            val code = userOTP.text.toString()
            if (code.isEmpty())
                showToast("ENTER A VALID CODE")
            else {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
                signInWithPhoneAuthCredential(credential)
            }
        }

        changeNumber.setOnClickListener {
            NUMBER = ""
            numberLayout.visibility = View.VISIBLE
            otpLayout.visibility = View.GONE
            otpWaiting = false
        }
    }

    override fun onBackPressed() {
        if (otpWaiting) {
            NUMBER = ""
            numberLayout.visibility = View.VISIBLE
            otpLayout.visibility = View.GONE
            otpWaiting = false
        } else
            super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(OTP_WAITING_STATE, otpWaiting)
        outState.putString(NUMBER_STATE, NUMBER)
        outState.putString(CODE_STATE, COUNTRY_CODE)
        outState.putString(VERIFICATION_ID_STATE, storedVerificationId)
        outState.putString(OTP_STATE, otpEntered)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        COUNTRY_CODE = savedInstanceState.getString(CODE_STATE, "+91")
        NUMBER = savedInstanceState.getString(NUMBER_STATE, "")
        otpWaiting = savedInstanceState.getBoolean(OTP_WAITING_STATE)
        storedVerificationId = savedInstanceState.getString(VERIFICATION_ID_STATE, "")
        otpEntered = savedInstanceState.getString(OTP_STATE, "")
        if (otpWaiting) {
            numberLayout.visibility = View.GONE
            otpLayout.visibility = View.VISIBLE
            otpLayout.requestFocus()
            userOTPRequest.text = "Please enter the verification code sent to $NUMBER"
            otpWaiting = true
            if (!(otpEntered.isNullOrEmpty() || otpEntered.isBlank()))
                userOTP.setText(otpEntered)
        } else {
            numberLayout.visibility = View.VISIBLE
            otpLayout.visibility = View.GONE
            userPhone.requestFocus()
            userPhone.setText(NUMBER)
            otpWaiting = false
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        progressDialog.show()
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val data = firestore.collection("users").document(user?.uid.toString()).get()
                    data.addOnCompleteListener {
                        if (it.result!!.exists()) {
                            startActivity(Intent(this, HomeActivity::class.java))
                        } else {
                            startActivity(Intent(this, UserDetailsActivity::class.java))
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showToast("INCORRECT CODE")
                    }
                }
            }
    }
}