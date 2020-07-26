package com.vikanshu.vaartalap.CallingActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.R

class CallingActivity : AppCompatActivity() {

    private lateinit var callerName: String
    private lateinit var callerNumber: String
    private lateinit var callerImage: String
    private lateinit var callerNameView: TextView
    private lateinit var callerNumberView: TextView
    private lateinit var callerImageView: ImageView
    private lateinit var acceptCallButton: Button
    private lateinit var rejectCallButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)
        val extras = intent.extras!!
        callerName = extras["name"].toString()
        callerNumber = extras["number"].toString()
        callerImage = extras["image"].toString()

        callerNameView = findViewById(R.id.name_caller)
        callerNumberView = findViewById(R.id.number_caller)
        callerImageView = findViewById(R.id.image_caller)
        acceptCallButton = findViewById(R.id.accept_caller)
        rejectCallButton = findViewById(R.id.reject_caller)

        setView()
    }

    private fun setView(){
        callerNameView.text = callerName
        callerNumberView.text = callerNumber
        Picasso.with(this).load(callerImage)
            .placeholder(this.getDrawable(R.drawable.icon_loading))
            .error(this.getDrawable(R.drawable.default_user))
            .into(callerImageView,object : Callback{
                override fun onSuccess() {}
                override fun onError() {
                    Toast.makeText(this@CallingActivity,"Error loading caller image",Toast.LENGTH_LONG).show()
                }
            })
    }
}