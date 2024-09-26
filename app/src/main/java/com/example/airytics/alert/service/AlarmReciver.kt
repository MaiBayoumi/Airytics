package com.example.airytics.alert.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.airytics.hostedactivity.view.HostedActivity
import com.example.airytics.model.AlarmItem
import com.example.airytics.R
import com.example.airytics.utilities.Constants
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.DelicateCoroutinesApi

class AlarmReciver: BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        val alertAction = intent?.action
        val alert = intent?.getSerializableExtra(Constants.ALERT_KEY) as? AlarmItem

        Log.d("HASSAN", "Received action: $alertAction")
        Log.d("HASSAN", "Received alert: $alert")


        // If action is null, log it
        if (alertAction == null) {
            Log.e("HASSAN", "Received null action!")
            return
        }

        when (alertAction) {
            Constants.ALERT_ACTION_NOTIFICATION -> {
                val showNotification = context?.getSharedPreferences(
                    Constants.SETTINGS_SHARED_PREFERENCE_NAME,
                    Context.MODE_PRIVATE
                )
                    ?.getBoolean(Constants.NOTIFICATION_KEY, false)
                if (showNotification == true) {
                    showNotification(context, alert)
                }
            }

            else -> Log.e("HASSAN", "Unknown action: $alertAction")
        }

        alert?.let {

        }
    }

    @SuppressLint("ServiceCast")
    private fun createChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                Constants.CHANNEL_ID.toString(),
                Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = Constants.CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun showNotification(context: Context?, alert: AlarmItem?) {
        val intent = Intent(context, HostedActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createChannel(context)

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationText = "Let's check the weather ${alert?.kind ?: ""}"

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_ID.toString())
            .setContentTitle("Airytics Alert")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Constants.CHANNEL_ID, notification)
    }
}