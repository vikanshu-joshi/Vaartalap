package com.vikanshu.vaartalap.CallingActivity

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.R

import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

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
    private lateinit var channelName: String

    private lateinit var prefs: SharedPreferences

    private lateinit var myName: String
    private lateinit var myNumber: String
    private lateinit var myImage: String
    private lateinit var myUID: String

    private lateinit var incomingCallLayout: LinearLayout
    private lateinit var videoChatLayout: ConstraintLayout
    private lateinit var progressBar: ConstraintLayout

    private lateinit var ringtone: String
    private var screenshotsAllowed = false

    private lateinit var firebaseFirestore: FirebaseFirestore

    private var callAccepted = false
    private var audioMuted = false
    private var videoDisabled = false

    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }
        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            runOnUiThread { onRemoteUserVideoMuted(uid, muted) }
        }
    }

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
        channelName = extras["channel"].toString()


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
        incomingCallLayout = findViewById(R.id.incoming_call_layout)
        videoChatLayout = findViewById(R.id.activity_video_chat_view)
        progressBar = findViewById(R.id.progressBarLayout)

        incomingCallLayout.visibility = View.VISIBLE
        videoChatLayout.visibility = View.GONE
        progressBar.visibility = View.GONE

        setView()

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initAgoraEngineAndJoinChannel()
        }

        firebaseFirestore = FirebaseFirestore.getInstance()

        acceptCallButton.setOnClickListener { acceptCall() }
        rejectCallButton.setOnClickListener { rejectCall() }
    }

    private fun acceptCall(){
        callAccepted = true
        firebaseFirestore.collection("users").document(myNumber).update("status","BUSY")
        firebaseFirestore.collection("users").document(myNumber)
            .collection("LOGS").document(callID).update("status","A")
        incomingCallLayout.visibility = View.GONE
        videoChatLayout.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        initAgoraEngineAndJoinChannel()
    }

    private fun rejectCall(){
        incomingCallLayout.visibility = View.GONE
        videoChatLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        firebaseFirestore.collection("users").document(myNumber)
            .update("status","IDLE").addOnSuccessListener {
                firebaseFirestore.collection("users").document(myNumber)
                    .collection("LOGS").document(callID).update("status","R").addOnSuccessListener {
                        this.finish()
                    }
            }
    }

    override fun onBackPressed() {
        if(callAccepted){
            Toast.makeText(this,"Please Wait......",Toast.LENGTH_LONG).show()
        }else{
            rejectCall()
        }
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

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupVideoProfile()
        setupLocalVideo()
        joinChannel()
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(LOG_TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode)

        when (requestCode) {
            PERMISSION_REQ_ID_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO)
                    finish()
                }
            }
            PERMISSION_REQ_ID_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel()
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA)
                    finish()
                }
            }
        }
    }

    private fun showLongToast(msg: String) {
        this.runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(callAccepted){
            leaveChannel()
            RtcEngine.destroy()
            mRtcEngine = null
        }
    }

    fun onLocalVideoMuteClicked(view: View) {
        videoDisabled = !videoDisabled
        val iv = view as ImageView
//        if (iv.isSelected) {
//            iv.isSelected = false
//            iv.clearColorFilter()
//        } else {
//            iv.isSelected = true
//            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
//        }
        if(videoDisabled){
            iv.setImageDrawable(this.getDrawable(R.drawable.video_disabled))
        }else{
            iv.setImageDrawable(this.getDrawable(R.drawable.video_enabled))
        }
        mRtcEngine!!.muteLocalVideoStream(videoDisabled)

//        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
//        val surfaceView = container.getChildAt(0) as SurfaceView
//        surfaceView.setZOrderMediaOverlay(!iv.isSelected)
//        surfaceView.visibility = if (iv.isSelected) View.GONE else View.VISIBLE
    }

    fun onLocalAudioMuteClicked(view: View) {
        audioMuted = !audioMuted
        val iv = view as ImageView
//        if (iv.isSelected) {
//            iv.isSelected = false
//            iv.clearColorFilter()
//        } else {
//            iv.isSelected = true
//            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
//        }
        if(audioMuted){
            iv.setImageDrawable(this.getDrawable(R.drawable.mic_disabled))
        }else{
            iv.setImageDrawable(this.getDrawable(R.drawable.mic_enabled))
        }
        mRtcEngine!!.muteLocalAudioStream(audioMuted)
    }

    fun onSwitchCameraClicked(view: View) {
        mRtcEngine!!.switchCamera()
    }

    fun onEndCallClicked(view: View) {
        finish()
    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine =
                RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {
            Log.e(LOG_TAG, Log.getStackTraceString(e))

            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(
                    e
                )
            )
        }
    }

    private fun setupVideoProfile() {
        mRtcEngine!!.enableVideo()
        mRtcEngine!!.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x480,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun setupLocalVideo() {
        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FILL, 0))
    }

    private fun joinChannel() {
        var token: String? = getString(R.string.agora_access_token)
        if (token!!.isEmpty()) {
            token = null
        }
        mRtcEngine!!.joinChannel(
            token,
            channelName,
            "",
            0
        )
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
        if (container.childCount >= 1) {
            return
        }
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        container.addView(surfaceView)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FILL, uid))
        surfaceView.tag = uid
    }

    private fun leaveChannel() {
        mRtcEngine?.leaveChannel()
    }

    private fun onRemoteUserLeft() {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
        container.removeAllViews()
    }

    private fun onRemoteUserVideoMuted(uid: Int, muted: Boolean) {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)

        val surfaceView = container.getChildAt(0) as SurfaceView

        val tag = surfaceView.tag
        if (tag != null && tag as Int == uid) {
            surfaceView.visibility = if (muted) View.GONE else View.VISIBLE
        }
    }

    companion object {

        private val LOG_TAG = IncomingCallActivity::class.java.simpleName

        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }
}