package com.vikanshu.vaartalap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vikanshu.vaartalap.HomeActivity.HomeActivity
import com.vikanshu.vaartalap.LoginActivity.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        startActivity(Intent(this,LoginActivity::class.java))
    }
}