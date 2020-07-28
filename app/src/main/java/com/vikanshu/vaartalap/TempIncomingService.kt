package com.vikanshu.vaartalap

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.vikanshu.vaartalap.CallingActivity.IncomingCallActivity
import java.util.*

class TempIncomingService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val myNumber = prefs.getString(getString(R.string.preference_key_number), "").toString()
        FirebaseFirestore.getInstance().collection("INCOMING")
            .document(myNumber).addSnapshotListener { value, error ->
                if (value!!.exists()) {
                    if (!prefs.getBoolean(getString(R.string.preference_key_status), false)) {
                        val i = Intent(this, IncomingCallActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        i.putExtra(
                            getString(R.string.call_data_name),
                            value.data?.get(getString(R.string.call_data_name)).toString()
                        )
                        i.putExtra(
                            getString(R.string.call_data_number),
                            value.data?.get(getString(R.string.call_data_number)).toString()
                        )
                        i.putExtra(
                            getString(R.string.call_data_uid),
                            value.data?.get(getString(R.string.call_data_uid)).toString()
                        )
                        i.putExtra(
                            getString(R.string.call_data_channel),
                            value.data?.get(getString(R.string.call_data_channel)).toString()
                        )
                        i.putExtra(
                            getString(R.string.call_data_image),
                            value.data?.get(getString(R.string.call_data_image)).toString()
                        )
                        startActivity(i)
                    }
                }
            }
        return START_STICKY
    }
}
