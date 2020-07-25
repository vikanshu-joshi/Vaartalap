package com.vikanshu.vaartalap

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class CallingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val number = UserDataSharedPref(applicationContext).getNumber()
        val data = HashMap<String, Any?>()
        data["token"] = token
        if (number != null)
            FirebaseFirestore.getInstance().collection("users").document(number).update(data)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}