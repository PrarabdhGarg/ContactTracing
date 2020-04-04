package com.example.covid_19

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception

class FirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val channel = "General"

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notification =NotificationCompat.Builder(this, channel)
            .setContentTitle(p0.notification!!.title)
            .setContentText(p0.notification!!.body)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)


        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(101, notification.build())
    }
}