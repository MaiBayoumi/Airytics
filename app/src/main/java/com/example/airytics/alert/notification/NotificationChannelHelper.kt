package com.example.airytics.alert.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.airytics.R
import com.example.airytics.hostedactivity.view.HostedActivity
import com.example.airytics.utilities.Constants


object NotificationChannelHelper {

//    private var channelIsCreated = false
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun createNotificationChannel(context: Context) {
//        if (!channelIsCreated) {
//            val name = Constants.CHANNEL_NAME
//            val descriptionText = Constants.CHANNEL_DESCRIPTION
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val myChannel = NotificationChannel(Constants.CHANNEL_ID.toString(), name, importance)
//            myChannel.description = descriptionText
//            val notificationManager =
//                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(myChannel)
//            channelIsCreated = true
//        }
//    }
//
//    fun createNotification(context: Context, description: String, zoneName: String): NotificationCompat.Builder {
//        val pendingIntent = createPendingIntent(context)
//        val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
//        builder.setSmallIcon(R.drawable.logo)
//        builder.setContentTitle("Airytics")
//        builder.setContentText("$description in $zoneName")
//        builder.setStyle(
//            NotificationCompat.BigTextStyle()
//                .bigText("$description in $zoneName")
//        )
//        builder.priority = NotificationCompat.PRIORITY_DEFAULT
//        builder.setContentIntent(pendingIntent)
//        builder.setAutoCancel(true)
//
//        return builder
//    }
//
//    private fun createPendingIntent(context: Context): PendingIntent {
//        val intent = Intent(context, HostedActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            action = "com.example.airytics.ACTION_NOTIFICATION"
//        }
//        return PendingIntent.getActivity(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Updated flags to ensure correct behavior
//        )
//    }

}