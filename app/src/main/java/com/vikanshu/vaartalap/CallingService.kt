package com.vikanshu.vaartalap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vikanshu.vaartalap.CallingActivity.IncomingCallActivity
import com.vikanshu.vaartalap.Database.LogDBHelper
import com.vikanshu.vaartalap.HomeActivity.HomeActivity
import com.vikanshu.vaartalap.model.LogsModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

open class CallingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val number = pref.getString(getString(R.string.preference_key_number), "")
        val data = HashMap<String, Any?>()
        data["token"] = token
        if (number != null)
            FirebaseFirestore.getInstance().collection("tokens").document(number).update(data)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val name = data[getString(R.string.call_data_name)]
        val number = data[getString(R.string.call_data_number)]
        val image = data[getString(R.string.call_data_image)]
        val channel = data[getString(R.string.call_data_channel)]
        val timestamp = data[getString(R.string.call_data_timestamp)]!!.toLong()
        val uid = data[getString(R.string.call_data_uid)]
        val minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timestamp)
        if (minutes <= 1) {
            val i = Intent(this, IncomingCallActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.putExtra(getString(R.string.call_data_name), name)
            i.putExtra(getString(R.string.call_data_number), number)
            i.putExtra(getString(R.string.call_data_uid), uid)
            i.putExtra(getString(R.string.call_data_channel), channel)
            i.putExtra(getString(R.string.call_data_image), image)
            startActivity(i)
        } else {
            val resultIntent = Intent(this, HomeActivity::class.java)

            LogDBHelper(this).store(
                LogsModel(
                    uid.toString(),
                    name.toString(),
                    number.toString(),
                    "M",
                    timestamp,
                    channel.toString(),
                    image.toString()
                )
            )

            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val resultPendingIntent = PendingIntent.getActivity(
                this,
                0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val mBuilder = NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.drawable.ic_notification)
            mBuilder.setContentTitle("Missed Call")
                .setContentText("from $name")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSound(Uri.parse("android.resource://" + this.packageName + "/" + R.raw.pikachu))
                .setContentIntent(resultPendingIntent)
            val mNotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH;
                val notificationChannel = NotificationChannel(
                    "1002",
                    "Missed Call Notifications",
                    importance
                );
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.RED
                notificationChannel.enableVibration(true);
                notificationChannel.vibrationPattern =
                    longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                mBuilder.setChannelId("1002");
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
            mNotificationManager.notify(0, mBuilder.build())
        }
    }
}