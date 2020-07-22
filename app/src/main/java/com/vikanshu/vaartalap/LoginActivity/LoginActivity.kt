package com.vikanshu.vaartalap.LoginActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.hbb20.CountryCodePicker
import com.vikanshu.vaartalap.R

class LoginActivity : AppCompatActivity() {

    private lateinit var countryCode : CountryCodePicker
    private lateinit var userPhone: EditText
    private lateinit var sendOTP: Button
    private var COUNTRY_CODE = "+91"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        countryCode = findViewById(R.id.countryCode)
        userPhone = findViewById(R.id.userPhoneLogin)
        sendOTP = findViewById(R.id.otp_login)

        countryCode.setOnCountryChangeListener {
            COUNTRY_CODE = countryCode.selectedCountryCodeWithPlus
            println(COUNTRY_CODE)
        }
    }
}