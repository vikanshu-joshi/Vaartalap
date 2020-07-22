package com.vikanshu.vaartalap.LoginActivity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.hbb20.CountryCodePicker
import com.vikanshu.vaartalap.R

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

        countryCode.setOnCountryChangeListener {
            COUNTRY_CODE = countryCode.selectedCountryCodeWithPlus
        }

        sendOTP.setOnClickListener {
            val number = userPhone.text.toString()
            if (number.isEmpty())
                showToast("ENTER A VALID MOBILE NUMBER")
            else {
                NUMBER = COUNTRY_CODE + number
                numberLayout.visibility = View.GONE
                otpLayout.visibility = View.VISIBLE
                otpLayout.requestFocus()
                userOTPRequest.text = "Please enter the verification code sent to $NUMBER"
                otpWaiting = true
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}