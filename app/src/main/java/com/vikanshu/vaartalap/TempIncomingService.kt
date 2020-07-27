package com.vikanshu.vaartalap

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.vikanshu.vaartalap.CallingActivity.CallingActivity
import java.util.*

class TempIncomingService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val number = prefs.getString(getString(R.string.preference_key_number),"").toString()
        FirebaseFirestore.getInstance().collection("INCOMING")
            .document(number).addSnapshotListener { value, error ->
                if(value!!.exists()){
                    val name = value.data?.get("name").toString()
                    val uid = value.data?.get("uid").toString()
                    val image = value.data?.get("image").toString()
                    val channel = value.data?.get("channel").toString()
                    val type = "I"
                    val i = Intent(this,CallingActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    i.putExtra("name",name)
                    i.putExtra("number",number)
                    i.putExtra("image",image)
                    i.putExtra("channel",channel)
                    i.putExtra("type",type)
                    i.putExtra("uid",uid)
                    startActivity(i)
                }
            }
        return START_STICKY
    }
}
