package com.vikanshu.vaartalap

import android.content.Intent
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vikanshu.vaartalap.CallingActivity.IncomingCallActivity

open class CallingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val number = pref.getString(getString(R.string.preference_key_number),"")
        val data = HashMap<String, Any?>()
        data["token"] = token
        if (number != null)
            FirebaseFirestore.getInstance().collection("tokens").document(number).update(data)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        println("new call")
        val data = message.data
        val name = data[getString(R.string.call_data_name)]
        val number = data[getString(R.string.call_data_number)]
        val image = data[getString(R.string.call_data_image)]
        val channel = data[getString(R.string.call_data_channel)]
        val timestamp = data[getString(R.string.call_data_timestamp)]!!.toLong()
        val uid = data[getString(R.string.call_data_uid)]

        if(true){
            val i = Intent(this, IncomingCallActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.putExtra(getString(R.string.call_data_name), name)
            i.putExtra(getString(R.string.call_data_number), number)
            i.putExtra(getString(R.string.call_data_uid), uid)
            i.putExtra(getString(R.string.call_data_channel), channel)
            i.putExtra(getString(R.string.call_data_image), image)
            startActivity(i)
        }
    }
}