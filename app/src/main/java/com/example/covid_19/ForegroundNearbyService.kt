package com.example.covid_19

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener


class ForegroundNearbyService: Service() {
    private val channelId = "ForegroundNearbyService"
    private val TAG = "ForegroundService"

    @ExperimentalStdlibApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build()
            Log.d(TAG, "onStartCommand called")
            val messageToBePublished = intent!!.getStringExtra("message")
            Log.d(TAG, "Message = $messageToBePublished")
            val message = Message(messageToBePublished.encodeToByteArray())
            val messageListener = object: MessageListener() {
                override fun onFound(message:Message) {
                    Log.d(TAG, "Found message: " + String(message.getContent()))
                    Toast.makeText(applicationContext, "Found message: " + String(message.getContent()), Toast.LENGTH_LONG).show()
                }
                override fun onLost(message:Message) {
                    Log.d(TAG, "Lost sight of message: " + String(message.getContent()))
                    Toast.makeText(applicationContext, "Lost message: " + String(message.getContent()), Toast.LENGTH_LONG).show()
                }
            }
            Nearby.getMessagesClient(this).publish(message)
            Nearby.getMessagesClient(this).subscribe(messageListener)
            startForeground(1, notification)
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}