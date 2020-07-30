package com.vikanshu.vaartalap

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
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
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.BLUETOOTH
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
//        offlineFeatures()
        auth = FirebaseAuth.getInstance()

        if (!hasAllPermissions(this, *permissions)) {// if permissions not granted then ask for them
            ActivityCompat.requestPermissions(this, permissions, 21023)
        } else {
            overlay()
        }
    }

    // function for result of asked permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            21023 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED
                    && grantResults[5] == PackageManager.PERMISSION_GRANTED
                    && grantResults[6] == PackageManager.PERMISSION_GRANTED
                    && grantResults[7] == PackageManager.PERMISSION_GRANTED
                    && grantResults[8] == PackageManager.PERMISSION_GRANTED
                    && grantResults[9] == PackageManager.PERMISSION_GRANTED
                    && grantResults[10] == PackageManager.PERMISSION_GRANTED
                    && grantResults[11] == PackageManager.PERMISSION_GRANTED
                ) {
                    overlay()
                } else {
                    Toast.makeText(
                        this,
                        "APP NEEDS ALL THE PERMISSIONS TO CONTINUE",
                        Toast.LENGTH_LONG
                    ).show()
                    this.finish()
                }
            }
        }
    }

    override fun onResume() {
        overlay()
        super.onResume()
    }

    private fun overlay() {
        if (SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                move()
            } else {
                Toast.makeText(this, "ALLOW VAARTALAP TO SHOW OVER OTHER APPS", Toast.LENGTH_LONG)
                    .show()
                val intent =
                    Intent(ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SYSTEM_ALERT_WINDOW
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this,
                    "APP NEEDS ALL THE PERMISSIONS TO CONTINUE",
                    Toast.LENGTH_LONG
                ).show()
                this.finish()
            }else{
                move()
            }
        }
    }

    private fun move() {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            this.finish()
        } else {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val number = pref.getString(getString(R.string.preference_key_number), "")
            firestore = FirebaseFirestore.getInstance()
            val data = firestore.collection("users").document(number.toString()).get()
            data.addOnCompleteListener {
                if (it.result!!.exists()) {
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    startActivity(Intent(this, UserDetailsActivity::class.java))
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

    private fun offlineFeatures() {
        val settings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
        val builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Integer.MAX_VALUE.toLong()))
        val built = builder.build()
        built.setIndicatorsEnabled(false)
        built.isLoggingEnabled = true
        Picasso.setSingletonInstance(built)
    }
}