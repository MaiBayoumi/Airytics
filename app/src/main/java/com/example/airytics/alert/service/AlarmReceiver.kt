package com.example.airytics.alert.service

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.airytics.alert.viewmodel.AlertViewModel
import com.example.airytics.database.LocalDataSource
import com.example.airytics.hostedactivity.view.TAG
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Repo
import com.example.airytics.network.RemoteDataSource
import com.example.airytics.sharedpref.SettingSharedPref
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                if (context != null) {
                    showAlarmDialog(context)
                }
            }
        }

        alert?.let {
            val repo = Repo.getInstance(
                RemoteDataSource,
                LocationClient.getInstance(
                    LocationServices.getFusedLocationProviderClient(context!!)
                ),
                LocalDataSource.getInstance(context),
                SettingSharedPref.getInstance(context)
            )

            val viewModel = AlertViewModel(repo)
            viewModel.deleteAlarm(it)
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
            notificationChannel.description = Constants.CHANNEL_DESCRIPTION
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

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Correct way to retrieve string resource
        val notificationText = context?.getString(R.string.check)

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_NAME)
            .setContentTitle("Airytics")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.logo2)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        // Play notification sound using MediaPlayer
        val mediaPlayer = MediaPlayer.create(context, R.raw.pop_up)
        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
        }

        // Handle possible errors during playback and release resources
        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            mp.release()
            true
        }

        notificationManager.notify(Constants.CHANNEL_ID, notification)
    }

    private fun showAlarm(context: Context?) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = Constants.ALERT_ACTION_ALARM
        context?.sendBroadcast(intent)
    }

    @SuppressLint("InflateParams")
    private fun showAlarmDialog(context: Context) {
        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_alarm, null)
//        val dialogMessage = dialogView.findViewById<TextView>(R.id.alert_description)
        //val zoneNameTV = dialogView.findViewById<TextView>(R.id.zoneName)
        val dialogOkButton = dialogView.findViewById<Button>(R.id.alert_stop)

        // Set dialog text
//        dialogMessage.text = messageFromApi
//        zoneNameTV.text = zoneName

        // Create media player for the alarm sound
        val mediaPlayer = MediaPlayer.create(context, R.raw.alert)

        // Build and show the dialog
        val builder = AlertDialog.Builder(context, R.style.MyCustomAlertDialogStyle)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.setCancelable(false)

        dialog.setOnShowListener {
            mediaPlayer.start()
            mediaPlayer.isLooping = true
        }

        // Set window type to show the dialog over other apps
        val window = dialog.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            window?.setType(WindowManager.LayoutParams.TYPE_PHONE)
        }
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.TOP)

        dialog.show()

        // Coroutine to auto-dismiss the dialog after 15 seconds
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(15000)
                withContext(Dispatchers.Main) {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                       showNotification(context)
                    }
                }
            } catch (_: Exception) {
            } finally {
                cancel()
            }
        }

        // Handle stop button click
        dialogOkButton.setOnClickListener {
            dialog.dismiss()
        }

        // Stop the media player when the dialog is dismissed
        dialog.setOnDismissListener {
            mediaPlayer.stop()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        }
    }

}
