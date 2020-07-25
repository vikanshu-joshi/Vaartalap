package com.vikanshu.vaartalap

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class FirebaseMessagingVaartalap : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        println("service : $token")
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}