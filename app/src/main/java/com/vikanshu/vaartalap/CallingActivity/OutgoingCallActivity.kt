package com.vikanshu.vaartalap.CallingActivity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.R
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

class OutgoingCallActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var callerName: String
    private lateinit var callerNumber: String
    private lateinit var channel: String
    private lateinit var callerImage: String
    private lateinit var callerUid: String

    private lateinit var callerNameView: TextView
    private lateinit var callerNumberView: TextView
    private lateinit var callerImageView: ImageView
    private lateinit var rejectCallButton: ImageView

    private lateinit var outgoingCallLayout: LinearLayout
    private lateinit var videoChatLayout: ConstraintLayout

    private lateinit var outgoingVideoStatus: TextView
    private lateinit var outgoingAudioStatus: TextView

    private lateinit var audioCallImageView: ImageView

    private lateinit var audioMutedView: TextView

    private lateinit var prefs: SharedPreferences

    private lateinit var myName: String
    private lateinit var myNumber: String
    private lateinit var myImage: String
    private lateinit var myUID: String

    private var screenshotsAllowed = false

    private var videoDisabled = false
    private var audioMuted = false

    private var callConnected = false

    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onUserJoined(p0: Int, p1: Int) {
            runOnUiThread {
                outgoingCallLayout.visibility = View.GONE
                videoChatLayout.isClickable = true
                videoChatLayout.visibility = View.VISIBLE
            }
            super.onUserJoined(p0, p1)
        }
        
        override fun onRemoteAudioStateChanged(id: Int, state: Int, reason: Int, p3: Int) {
            when (reason) {
                0 -> {
                    runOnUiThread {
                        outgoingAudioStatus.text = ""
                        outgoingAudioStatus.visibility = View.INVISIBLE
                    }
                }
                1 -> {
                    runOnUiThread {
                        outgoingAudioStatus.text = "Audio Network Congestion"
                        outgoingAudioStatus.visibility = View.INVISIBLE
                    }
                }
                2 -> {
                    runOnUiThread {
                        outgoingAudioStatus.text = ""
                        outgoingAudioStatus.visibility = View.INVISIBLE
                    }
                }
                5 -> {
                    runOnUiThread {
                        audioMuted = true
                        audioMutedView.visibility = View.VISIBLE
                    }
                }
                6 -> {
                    runOnUiThread {
                        audioMuted = false
                        audioMutedView.visibility = View.INVISIBLE
                    }
                }
                else -> {
                }
            }
            super.onRemoteAudioStateChanged(id, state, reason, p3)
        }

        override fun onRemoteVideoStateChanged(id: Int, state: Int, reason: Int, p3: Int) {
            when (reason) {
                0 -> {
                    runOnUiThread {
                        outgoingVideoStatus.text = ""
                        outgoingVideoStatus.visibility = View.INVISIBLE
                    }
                }
                1 -> {
                    runOnUiThread {
                        outgoingVideoStatus.text = "Video Network Congestion"
                        outgoingVideoStatus.visibility = View.VISIBLE
                    }
                }
                2 -> {
                    runOnUiThread {
                        outgoingVideoStatus.text = ""
                        outgoingVideoStatus.visibility = View.INVISIBLE
                    }
                }
                5 -> {
                    runOnUiThread {
                        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
                        container.visibility = View.INVISIBLE
                    }
                }
                6 -> {
                    runOnUiThread {
                        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
                        container.visibility = View.VISIBLE
                    }
                }
                7 -> {
                    runOnUiThread {
                        Toast.makeText(
                            this@OutgoingCallActivity,
                            "CALL DISCONNECTED",
                            Toast.LENGTH_LONG
                        ).show()
                        this@OutgoingCallActivity.finish()
                    }
                }
                8 -> {
                    runOnUiThread {
                        Toast.makeText(
                            this@OutgoingCallActivity,
                            "BAD NETWORK, CONVERTING TO AUDIO CALL",
                            Toast.LENGTH_LONG
                        ).show()
                        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
                        container.visibility = View.INVISIBLE
                    }
                }
                9 -> {
                    runOnUiThread {
                        Toast.makeText(
                            this@OutgoingCallActivity,
                            "NETWORK BACK, RETURNING TO AUDIO CALL",
                            Toast.LENGTH_LONG
                        ).show()
                        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
                        container.visibility = View.VISIBLE
                    }
                }
                else -> {
                }
            }
            super.onRemoteVideoStateChanged(id, state, reason, p3)
        }

        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@OutgoingCallActivity, "CALL DISCONNECTED", Toast.LENGTH_LONG)
                    .show()
                this@OutgoingCallActivity.finish()
            }
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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_outgoing_call)
        fetchAllData()
        findViews()
        setViews()
        firestore = FirebaseFirestore.getInstance()
        val data = HashMap<String,Any>()
        data[getString(R.string.call_data_name)] = myName
        data[getString(R.string.call_data_number)] = myNumber
        data[getString(R.string.call_data_image)] = myImage
        data[getString(R.string.call_data_uid)] = myUID
        data[getString(R.string.call_data_channel)] = channel
        data[getString(R.string.call_data_timestamp)] = System.currentTimeMillis()
        firestore.collection("INCOMING").document(callerNumber)
            .set(data).addOnCompleteListener {
                if(it.isSuccessful){
                    rejectCallButton.visibility = View.VISIBLE
                    initAgoraEngineAndJoinChannel()
                    callConnected = true
                }else{
                    Toast.makeText(this,"Failed To Make Call",Toast.LENGTH_LONG).show()
                }
            }

        rejectCallButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchAllData() {
        val extras = intent.extras
        callerName = extras?.get(getString(R.string.call_data_name)).toString()
        callerNumber = extras?.get(getString(R.string.call_data_number)).toString()
        callerImage = extras?.get(getString(R.string.call_data_image)).toString()
        callerUid = extras?.get(getString(R.string.call_data_uid)).toString()
        channel = extras?.get(getString(R.string.call_data_channel)).toString()

        myName = prefs.getString(getString(R.string.preference_key_name), "").toString()
        myNumber = prefs.getString(getString(R.string.preference_key_number), "").toString()
        myImage = prefs.getString(getString(R.string.preference_key_image), "").toString()
        myUID = prefs.getString(getString(R.string.preference_key_uid), "").toString()
    }

    private fun findViews() {
        callerNameView = findViewById(R.id.name_outgoing)
        callerNumberView = findViewById(R.id.number_outgoing)
        callerImageView = findViewById(R.id.image_outgoing)

        rejectCallButton = findViewById(R.id.end_outgoing_call)

        outgoingCallLayout = findViewById(R.id.outgoing_call_container)
        videoChatLayout = findViewById(R.id.activity_video_chat_view)

        outgoingAudioStatus = findViewById(R.id.audio_status_outgoing)
        outgoingVideoStatus = findViewById(R.id.video_status_outgoing)

        audioCallImageView = findViewById(R.id.image_call_going)
        audioMutedView = findViewById(R.id.remote_call_muted)
    }

    private fun setViews() {
        outgoingCallLayout.visibility = View.VISIBLE
        videoChatLayout.visibility = View.INVISIBLE
        videoChatLayout.isClickable = false
        callerNameView.text = callerName
        callerNumberView.text = callerNumber
        Picasso.with(this).load(callerImage)
            .placeholder(resources.getDrawable(R.drawable.icon_loading))
            .error(resources.getDrawable(R.drawable.default_user))
            .into(callerImageView)
        Picasso.with(this).load(callerImage)
            .placeholder(resources.getDrawable(R.drawable.icon_loading))
            .error(resources.getDrawable(R.drawable.default_user))
            .into(audioCallImageView)
    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupVideoProfile()
        setupLocalVideo()
        joinChannel()
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

    override fun onBackPressed() {
        if (callConnected) {
            leaveChannel()
            RtcEngine.destroy()
            mRtcEngine = null
        }
        finish()
    }

    override fun onDestroy() {
        if (callConnected) {
            leaveChannel()
            RtcEngine.destroy()
            mRtcEngine = null
        }
        super.onDestroy()
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
        mRtcEngine!!.adjustPlaybackSignalVolume(150)
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
            channel,
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

    companion object {

        private val LOG_TAG = OutgoingCallActivity::class.java.simpleName

        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }
}