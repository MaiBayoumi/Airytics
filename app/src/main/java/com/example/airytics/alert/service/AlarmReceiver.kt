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
import com.example.airytics.R
import com.example.airytics.utilities.Constants
import androidx.core.app.NotificationCompat
import com.example.airytics.model.AlarmItem

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("HASSAN", "onReceive called at: ${System.currentTimeMillis()}")
        val alertAction = intent?.action
        val alert = intent?.getParcelableExtra<AlarmItem>(Constants.ALERT_KEY)

        Log.d("HASSAN", "Received action: $alertAction")
        Log.d("HASSAN", "Received alert: $alert")

        when(alertAction)
        {
            Constants.ALERT_ACTION_NOTIFICATION -> {
                val showNotification = context?.getSharedPreferences(Constants.SETTINGS_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)?.getBoolean(Constants.NOTIFICATION_KEY, false)
                if(showNotification == true)
                {
                    showNotification(context)
                }
            }
            Constants.ALERT_ACTION_ALARM -> {
                showAlarm(context)
            }
        }

        alert?.let {
//            GlobalScope.launch {
//                repository.deleteAlert(alert)
//            }
        }

    }

    @SuppressLint("ServiceCast")
        private fun createChannel(context: Context?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationChannel = NotificationChannel(
                    Constants.CHANNEL_NAME,
                    Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }


        private fun showNotification(context: Context?) {
            val intent = Intent(context, HostedActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            createChannel(context)
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification = NotificationCompat.Builder(context, Constants.CHANNEL_NAME)
                .setContentTitle("Airytics")
                .setContentText("Let's check the weather")
                .setSmallIcon(R.drawable.logo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(Constants.CHANNEL_ID, notification)

        }

    private fun showAlarm(context: Context?) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = Constants.ALERT_ACTION_ALARM
        context?.sendBroadcast(intent)
    }


}
