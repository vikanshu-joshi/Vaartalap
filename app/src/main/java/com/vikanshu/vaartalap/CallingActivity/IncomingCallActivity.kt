package com.vikanshu.vaartalap.CallingActivity

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.R
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var callerName: String
    private lateinit var callerNumber: String
    private lateinit var callerImage: String
    private lateinit var callerUID: String
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
    private lateinit var incomingVideoStatus: TextView
    private lateinit var incomingAudioStatus: TextView
    private lateinit var audioCallImageView: ImageView
    private lateinit var audioMutedView: TextView
    private var screenshotsAllowed = false
    private var videoDisabled = false
    private var audioMuted = false
    private var callAccepted = false
    private lateinit var mediaPlayer: MediaPlayer
    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {


        override fun onRemoteAudioStateChanged(id: Int, state: Int, reason: Int, p3: Int) {
            when (reason) {
                0 -> {
                    runOnUiThread {
                        incomingAudioStatus.text = ""
                        incomingAudioStatus.visibility = View.INVISIBLE
                    }
                }
                1 -> {
                    runOnUiThread {
                        incomingAudioStatus.text = "Audio Network Congestion"
                        incomingAudioStatus.visibility = View.INVISIBLE
                    }
                }
                2 -> {
                    runOnUiThread {
                        incomingAudioStatus.text = ""
                        incomingAudioStatus.visibility = View.INVISIBLE
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
                        incomingVideoStatus.text = ""
                        incomingVideoStatus.visibility = View.INVISIBLE
                    }
                }
                1 -> {
                    runOnUiThread {
                        incomingVideoStatus.text = "Video Network Congestion"
                        incomingVideoStatus.visibility = View.VISIBLE
                    }
                }
                2 -> {
                    runOnUiThread {
                        incomingVideoStatus.text = ""
                        incomingVideoStatus.visibility = View.INVISIBLE
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
                            this@IncomingCallActivity,
                            "CALL DISCONNECTED",
                            Toast.LENGTH_LONG
                        ).show()
                        this@IncomingCallActivity.finish()
                    }
                }
                8 -> {
                    runOnUiThread {
                        Toast.makeText(
                            this@IncomingCallActivity,
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
                            this@IncomingCallActivity,
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
                Toast.makeText(this@IncomingCallActivity, "CALL DISCONNECTED", Toast.LENGTH_LONG)
                    .show()
                this@IncomingCallActivity.finish()
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
        setContentView(R.layout.activity_incoming_call)
        fetchAllData()
        findViews()
        setViews()
        val audioManager: AudioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0)
        val tone = prefs.getString(getString(R.string.preference_key_ringtone), "0")
        mediaPlayer = when (tone) {
            "0" -> {
                MediaPlayer.create(this, R.raw.child_laugh)
            }
            "1" -> {
                MediaPlayer.create(this, R.raw.iphone_call)
            }
            "2" -> {
                MediaPlayer.create(this, R.raw.hangouts_call)
            }
            else -> {
                MediaPlayer.create(this, R.raw.mi_call)
            }
        }
        acceptCallButton.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.release()
            acceptCall()
            val data = HashMap<String, Any>()
            data[getString(R.string.call_log_data_name)] = callerName
            data[getString(R.string.call_log_data_number)] = callerNumber
            data[getString(R.string.call_log_data_image)] = callerImage
            data[getString(R.string.call_log_data_type)] = "A"
            data[getString(R.string.call_log_data_timestamp)] = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("users").document(myNumber)
                .collection("logs").document(channelName).set(data)
        }
        rejectCallButton.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.release()
            val data = HashMap<String, Any>()
            data[getString(R.string.call_log_data_name)] = callerName
            data[getString(R.string.call_log_data_number)] = callerNumber
            data[getString(R.string.call_log_data_image)] = callerImage
            data[getString(R.string.call_log_data_type)] = "R"
            data[getString(R.string.call_log_data_timestamp)] = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("users").document(myNumber)
                .collection("logs").document(channelName).set(data)
                .addOnCompleteListener { this.finish() }
        }
    }

    override fun onStart() {
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        super.onStart()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun acceptCall() {
        callAccepted = true
        incomingCallLayout.visibility = View.GONE
        videoChatLayout.visibility = View.VISIBLE
        Picasso.with(this).load(callerImage)
            .placeholder(resources.getDrawable(R.drawable.icon_loading))
            .error(resources.getDrawable(R.drawable.default_user))
            .into(audioCallImageView)
        initAgoraEngineAndJoinChannel()
    }

    private fun fetchAllData() {
        val extras = intent.extras
        callerName = extras?.get(getString(R.string.call_data_name)).toString()
        callerNumber = extras?.get(getString(R.string.call_data_number)).toString()
        callerImage = extras?.get(getString(R.string.call_data_image)).toString()
        callerUID = extras?.get(getString(R.string.call_data_uid)).toString()
        channelName = extras?.get(getString(R.string.call_data_channel)).toString()

        myName = prefs.getString(getString(R.string.preference_key_name), "").toString()
        myNumber = prefs.getString(getString(R.string.preference_key_number), "").toString()
        myImage = prefs.getString(getString(R.string.preference_key_image), "").toString()
        myUID = prefs.getString(getString(R.string.preference_key_uid), "").toString()
    }

    private fun findViews() {
        callerNameView = findViewById(R.id.name_caller)
        callerNumberView = findViewById(R.id.number_caller)
        callerImageView = findViewById(R.id.image_caller)

        acceptCallButton = findViewById(R.id.accept_caller)
        rejectCallButton = findViewById(R.id.reject_caller)

        incomingCallLayout = findViewById(R.id.incoming_call_layout)
        videoChatLayout = findViewById(R.id.activity_video_chat_view)

        incomingAudioStatus = findViewById(R.id.audio_status_incoming)
        incomingVideoStatus = findViewById(R.id.video_status_incoming)

        audioCallImageView = findViewById(R.id.image_call_going)
        audioMutedView = findViewById(R.id.remote_call_muted)
    }

    private fun setViews() {
        incomingCallLayout.visibility = View.VISIBLE
        videoChatLayout.visibility = View.GONE
        callerNameView.text = callerName
        callerNumberView.text = callerNumber
        Picasso.with(this).load(callerImage)
            .placeholder(resources.getDrawable(R.drawable.icon_loading))
            .error(resources.getDrawable(R.drawable.default_user))
            .into(callerImageView)
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
        if (callAccepted) {
            leaveChannel()
            RtcEngine.destroy()
            mRtcEngine = null
        }
        finish()
    }

    override fun onDestroy() {
        if (callAccepted) {
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

    companion object {

        private val LOG_TAG = IncomingCallActivity::class.java.simpleName

        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }
}