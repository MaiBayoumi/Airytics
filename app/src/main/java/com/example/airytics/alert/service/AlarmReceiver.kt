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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.airytics.alert.viewmodel.AlertViewModel
import com.example.airytics.hostedactivity.view.TAG
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val alertAction = intent?.action
        val alert = intent?.getParcelableExtra<AlarmItem>(Constants.ALERT_KEY)


        when (alertAction) {
            Constants.ALERT_ACTION_NOTIFICATION -> {
                // Check for Android 13+ notification permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permission = ContextCompat.checkSelfPermission(
                        context!!, android.Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Notification permission not granted.")
                        return
                    }
                }


                val sharedPrefs = context?.getSharedPreferences(
                    Constants.SETTINGS_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE
                )
                val showNotification = sharedPrefs?.getBoolean(
                    Constants.NOTIFICATION_KEY, true
                ) ?: true

                if (showNotification) {
                    showNotification(context)
                } else {
                    Log.d(TAG, "Notification is disabled in preferences.")
                }
            }

            Constants.ALERT_ACTION_ALARM -> {
                showAlarm(context)
            }
        }

        alert?.let {

        }
    }

    @SuppressLint("ServiceCast")
    private fun createChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationChannel = NotificationChannel(
                Constants.CHANNEL_NAME,
                Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description =Constants.CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun showNotification(context: Context?) {
        val intent = Intent(context, HostedActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createChannel(context)

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
