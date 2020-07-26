package com.vikanshu.vaartalap

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vikanshu.vaartalap.HomeActivity.HomeActivity
import com.vikanshu.vaartalap.LoginActivity.LoginActivity
import com.vikanshu.vaartalap.UserDetailsActivity.UserDetailsActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    private val permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()

        if (!hasAllPermissions(this, *permissions)) // if permissions not granted then ask for them
            ActivityCompat.requestPermissions(this, permissions, 21023)
        else{
            move()
        }
    }

    // function for result of asked permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            21023 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED
                    && grantResults[5] == PackageManager.PERMISSION_GRANTED
                    && grantResults[6] == PackageManager.PERMISSION_GRANTED) {
                    move()
                } else {
                    Toast.makeText(this,"APP NEEDS ALL THE PERMISSIONS TO CONTINUE",Toast.LENGTH_LONG).show()
                    this.finish()
                }
            }
        }
    }

    private fun move(){
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            this.finish()
        } else {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val number = pref.getString(getString(R.string.preference_key_number),"")
            firestore = FirebaseFirestore.getInstance()
            val data = firestore.collection("users").document(number.toString()).get()
            data.addOnCompleteListener {
                if(it.result!!.exists()){
                    startActivity(Intent(this,HomeActivity::class.java))
                }else{
                    startActivity(Intent(this,UserDetailsActivity::class.java))
                }
                this.finish()
            }
        }
    }

    // function to check whether all permissions are granted
    private fun hasAllPermissions(context: Context, vararg permissions: String): Boolean {
        for (i in permissions) {
            val res = context.checkCallingOrSelfPermission(i)
            if (res == PackageManager.PERMISSION_DENIED)
                return false
        }
        return true
    }
}