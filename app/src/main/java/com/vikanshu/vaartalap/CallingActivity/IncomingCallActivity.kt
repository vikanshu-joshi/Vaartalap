package com.vikanshu.vaartalap.CallingActivity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.R

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var callID: String
    private lateinit var callerName: String
    private lateinit var callerNumber: String
    private lateinit var callerImage: String
    private lateinit var callerUID: String
    private lateinit var callTypeView: TextView
    private lateinit var callerNameView: TextView
    private lateinit var callerNumberView: TextView
    private lateinit var callerImageView: ImageView
    private lateinit var acceptCallButton: Button
    private lateinit var rejectCallButton: Button

    private lateinit var prefs: SharedPreferences

    private lateinit var myName: String
    private lateinit var myNumber: String
    private lateinit var myImage: String
    private lateinit var myUID: String

    private lateinit var ringtone: String
    private var screenshotsAllowed = false

    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        screenshotsAllowed = prefs.getBoolean(getString(R.string.preference_key_screenshots),false)
        if(!screenshotsAllowed){
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(R.layout.activity_incoming_call)

        val extras = intent.extras!!
        callerName = extras["name"].toString()
        callerNumber = extras["number"].toString()
        callerImage = extras["image"].toString()
        callerUID = extras["uid"].toString()
        callID = extras["id"].toString()


        myName = prefs.getString(getString(R.string.preference_key_name),"").toString()
        myNumber = prefs.getString(getString(R.string.preference_key_number),"").toString()
        myImage = prefs.getString(getString(R.string.preference_key_image),"").toString()
        myUID = prefs.getString(getString(R.string.preference_key_uid),"").toString()
        ringtone = prefs.getString(getString(R.string.preference_key_ringtone),"R.raw.child_laugh").toString()

        callerNameView = findViewById(R.id.name_caller)
        callerNumberView = findViewById(R.id.number_caller)
        callerImageView = findViewById(R.id.image_caller)
        acceptCallButton = findViewById(R.id.accept_caller)
        rejectCallButton = findViewById(R.id.reject_caller)
        callTypeView = findViewById(R.id.call_type_caller)

        setView()

        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    private fun acceptCall(){
        firebaseFirestore.collection("users").document(myNumber).update("status","BUSY")
        firebaseFirestore.collection("users").document(myNumber)
            .collection("LOGS").document(callID).update("status","A")
    }

    private fun rejectCall(){
        firebaseFirestore.collection("users").document(myNumber).update("status","IDLE")
        firebaseFirestore.collection("users").document(myNumber)
            .collection("LOGS").document(callID).update("status","R")
    }

    private fun setView(){
        callerNameView.text = callerName
        callerNumberView.text = callerNumber
        callTypeView.text = getString(R.string.incoming)
        Picasso.with(this).load(callerImage)
            .placeholder(this.getDrawable(R.drawable.icon_loading))
            .error(this.getDrawable(R.drawable.default_user))
            .into(callerImageView,object : Callback{
                override fun onSuccess() {}
                override fun onError() {
                    Toast.makeText(this@IncomingCallActivity,"Error loading caller image",Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun establishConnection(){

    }
}