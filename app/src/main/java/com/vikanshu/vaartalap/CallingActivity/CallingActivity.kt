package com.vikanshu.vaartalap.CallingActivity

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
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

class CallingActivity : AppCompatActivity() {

//    private lateinit var callID: String
    private lateinit var callerName: String
    private lateinit var callerNumber: String
    private lateinit var callerImage: String
    private lateinit var callerUID: String
    private lateinit var callTypeView: TextView
    private lateinit var callerNameView: TextView
    private lateinit var callerNumberView: TextView
    private lateinit var callerImageView: ImageView
    private lateinit var acceptCallButton: ImageView
    private lateinit var rejectCallButton: ImageView
    private lateinit var channelName: String

    private lateinit var prefs: SharedPreferences

    private lateinit var myName: String
    private lateinit var myNumber: String
    private lateinit var myImage: String
    private lateinit var myUID: String

    private lateinit var incomingCallLayout: LinearLayout
    private lateinit var videoChatLayout: ConstraintLayout
    private lateinit var progressBar: ConstraintLayout
    private lateinit var videoChanged: TextView
    private lateinit var audioChanged: TextView
    private lateinit var callGoingImage: ImageView

    private lateinit var ringtone: String
    private var screenshotsAllowed = false

    private lateinit var callType: String

    private lateinit var firebaseFirestore: FirebaseFirestore

    private var callAccepted = false
    private var audioMuted = false
    private var videoDisabled = false

    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onRemoteAudioStateChanged(id: Int, state: Int, reason: Int, p3: Int) {
            when (reason) {
                0 -> {
                    runOnUiThread {
                        audioChanged.visibility = View.INVISIBLE
                    }
                }
                1 -> {
                    runOnUiThread {
                        audioChanged.text = getString(R.string.audio_network_congestion)
                        audioChanged.visibility = View.VISIBLE
                    }
                }
                2 -> {
                    runOnUiThread {
                        audioChanged.text = ""
                        audioChanged.visibility = View.INVISIBLE
                    }
                }
                3 -> {
                    runOnUiThread {
                        audioChanged.text = ""
                        audioChanged.visibility = View.INVISIBLE
                    }
                }
                4 -> {
                    runOnUiThread {
                        audioChanged.text = ""
                        audioChanged.visibility = View.INVISIBLE
                    }
                }
                5 -> {
                    runOnUiThread {
                        audioChanged.text = getString(R.string.audio_muted)
                        audioChanged.visibility = View.VISIBLE
                    }
                }
                6 -> {
                    runOnUiThread {
                        audioChanged.text = ""
                        audioChanged.visibility = View.INVISIBLE
                    }
                }
                7 -> {
                    runOnUiThread {
                        audioChanged.text = ""
                        audioChanged.visibility = View.INVISIBLE
                    }
                }
                else -> {
                    runOnUiThread {
                        audioChanged.text = ""
                        audioChanged.visibility = View.INVISIBLE
                    }
                }
            }
            super.onRemoteAudioStateChanged(id, state, reason, p3)
        }

        override fun onRemoteVideoStateChanged(id: Int, state: Int, reason: Int, p3: Int) {
            when (reason) {
                0 -> {
                    runOnUiThread {
//                        callGoingImage.visibility = View.INVISIBLE
                        videoChanged.visibility = View.INVISIBLE
                    }
                }
                1 -> {
                    runOnUiThread {
                        videoChanged.text = getString(R.string.video_network_congestion)
                        videoChanged.visibility = View.VISIBLE
                    }
                }
                2 -> {
                    runOnUiThread {
//                        callGoingImage.visibility = View.INVISIBLE
                        videoChanged.text = ""
                        videoChanged.visibility = View.INVISIBLE
                    }
                }
                3 -> {
                    runOnUiThread {
//                        callGoingImage.visibility = View.INVISIBLE
                        videoChanged.text = ""
                        videoChanged.visibility = View.INVISIBLE
                    }
                }
                4 -> {
                    runOnUiThread {
//                        callGoingImage.visibility = View.INVISIBLE
                        videoChanged.text = ""
                        videoChanged.visibility = View.INVISIBLE
                    }
                }
                5 -> {
                    runOnUiThread {
//                        callGoingImage.visibility = View.VISIBLE
                        videoChanged.text = getString(R.string.video_disabled)
                        videoChanged.visibility = View.VISIBLE
                        onRemoteUserVideoMuted(id, true)
                    }
                }
                6 -> {
                    runOnUiThread {
//                        callGoingImage.visibility = View.INVISIBLE
                        videoChanged.text = ""
                        videoChanged.visibility = View.INVISIBLE
                        onRemoteUserVideoMuted(id, false)
                    }
                }
                7 -> {
                    runOnUiThread {
//                        callGoingImage.visibility = View.VISIBLE
                        videoChanged.text = getString(R.string.user_offline)
                        videoChanged.visibility = View.VISIBLE
                    }
                }
                8 -> {
                    runOnUiThread {
//                        callGoingImage.visibility = View.INVISIBLE
                        videoChanged.text = getString(R.string.converting_to_audio)
                        videoChanged.visibility = View.VISIBLE
                    }
                }
                9 -> {
                    runOnUiThread {
                        Toast.makeText(
                            this@CallingActivity,
                            "Returning to video call",
                            Toast.LENGTH_LONG
                        ).show()
                        videoChanged.text = ""
                        videoChanged.visibility = View.INVISIBLE
                    }
                }
                else -> {
                    videoChanged.text = ""
                    runOnUiThread { videoChanged.visibility = View.INVISIBLE }
                }
            }
            super.onRemoteVideoStateChanged(id, state, reason, p3)
        }

        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        screenshotsAllowed = prefs.getBoolean(getString(R.string.preference_key_screenshots), false)
        if (!screenshotsAllowed) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        setContentView(R.layout.activity_incoming_call)

        val extras = intent.extras
        callerName = extras?.get("name").toString()
        callerNumber = extras?.get("number").toString()
        callerImage = extras?.get("image").toString()
        callerUID = extras?.get("uid").toString()
//        callID = extras?.get("id").toString()
        channelName = extras?.get("channel").toString()
        callType = extras?.get("type").toString()


        myName = prefs.getString(getString(R.string.preference_key_name), "").toString()
        myNumber = prefs.getString(getString(R.string.preference_key_number), "").toString()
        myImage = prefs.getString(getString(R.string.preference_key_image), "").toString()
        myUID = prefs.getString(getString(R.string.preference_key_uid), "").toString()
        ringtone = prefs.getString(getString(R.string.preference_key_ringtone), "R.raw.child_laugh")
            .toString()

        callerNameView = findViewById(R.id.name_caller)
        callerNumberView = findViewById(R.id.number_caller)
        callerImageView = findViewById(R.id.image_caller)
        acceptCallButton = findViewById(R.id.accept_caller)
        rejectCallButton = findViewById(R.id.reject_caller)
        callTypeView = findViewById(R.id.call_type_caller)
        incomingCallLayout = findViewById(R.id.incoming_call_layout)
        videoChatLayout = findViewById(R.id.activity_video_chat_view)
        progressBar = findViewById(R.id.progressBarLayout)
        videoChanged = findViewById(R.id.video_mute)
        audioChanged = findViewById(R.id.audio_mute)
        callGoingImage = findViewById(R.id.image_call_going)

        firebaseFirestore = FirebaseFirestore.getInstance()
        setView()
    }

    private fun acceptCall() {
        callAccepted = true
        incomingCallLayout.visibility = View.GONE
        videoChatLayout.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        initAgoraEngineAndJoinChannel()
    }

    private fun rejectCall() {
        incomingCallLayout.visibility = View.GONE
        videoChatLayout.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        this.finish()
//        firebaseFirestore.collection("users").document(myNumber)
//            .update("status", "IDLE").addOnSuccessListener {
//                firebaseFirestore.collection("users").document(myNumber)
//                    .collection("LOGS").document(callID).update("status", "R")
//                    .addOnSuccessListener {
//                        this.finish()
//                    }
//            }
    }

    override fun onBackPressed() {
        if (callAccepted) {
            Toast.makeText(this, "Please Wait......", Toast.LENGTH_LONG).show()
        } else {
            rejectCall()
        }
    }

    private fun setView() {
        if(callType == "I"){
            incomingCallLayout.visibility = View.VISIBLE
            videoChatLayout.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
            callerNameView.text = callerName
            callerNumberView.text = callerNumber
            callTypeView.text = getString(R.string.incoming)
            Picasso.with(this).load(callerImage)
                .placeholder(this.getDrawable(R.drawable.icon_loading))
                .error(this.getDrawable(R.drawable.default_user))
                .into(callerImageView, object : Callback {
                    override fun onSuccess() {}
                    override fun onError() {
                        Toast.makeText(
                            this@CallingActivity,
                            "Error loading caller image",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            acceptCallButton.setOnClickListener { acceptCall() }
            rejectCallButton.setOnClickListener { rejectCall() }
        }else{
            incomingCallLayout.visibility = View.GONE
            videoChatLayout.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            initAgoraEngineAndJoinChannel()
        }
        Picasso.with(this).load(callerImage)
            .placeholder(this.getDrawable(R.drawable.icon_loading))
            .error(this.getDrawable(R.drawable.default_user))
            .into(callGoingImage)
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

        if (callAccepted) {
            leaveChannel()
            RtcEngine.destroy()
            mRtcEngine = null
        }
    }

    fun onLocalVideoMuteClicked(view: View) {
        videoDisabled = !videoDisabled
        val iv = view as ImageView
        if (videoDisabled) {
            iv.setImageDrawable(this.getDrawable(R.drawable.video_disabled))
            iv.setBackgroundColor(resources.getColor(R.color.colorAccent))
        } else {
            iv.setImageDrawable(this.getDrawable(R.drawable.video_enabled))
            iv.setBackgroundColor(resources.getColor(R.color.white))
        }
        mRtcEngine!!.muteLocalVideoStream(videoDisabled)
    }

    fun onLocalAudioMuteClicked(view: View) {
        audioMuted = !audioMuted
        val iv = view as ImageView
        if (audioMuted) {
            iv.setImageDrawable(this.getDrawable(R.drawable.mic_disabled))
            iv.setBackgroundColor(resources.getColor(R.color.colorAccent))
        } else {
            iv.setImageDrawable(this.getDrawable(R.drawable.mic_enabled))
            iv.setBackgroundColor(resources.getColor(R.color.white))
        }
        mRtcEngine!!.muteLocalAudioStream(audioMuted)
    }

    fun onSwitchCameraClicked(view: View) {
        mRtcEngine!!.switchCamera()
    }

    fun onEndCallClicked(view: View) {
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
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

        private val LOG_TAG = CallingActivity::class.java.simpleName

        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }
}