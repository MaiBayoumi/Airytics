package com.example.airytics.alert.view

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.airytics.R
import com.example.airytics.alert.service.AlarmReceiver
import com.example.airytics.alert.viewmodel.AlertViewModel
import com.example.airytics.alert.viewmodel.AlertViewModelFactory
import com.example.airytics.database.LocalDataSource
import com.example.airytics.databinding.AlertDialogLayoutBinding
import com.example.airytics.databinding.FragmentAlertBinding
import com.example.airytics.location.LocationClient
import com.example.airytics.model.Repo
import com.example.airytics.network.RemoteDataSource
import com.example.airytics.model.AlarmItem
import com.example.airytics.sharedpref.SettingSharedPref
import com.example.airytics.utilities.Constants
import com.example.airytics.utilities.Functions
import com.example.airytics.utilities.PermissionUtility
import com.google.android.gms.location.LocationServices
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar


class AlertFragment : Fragment() {

    private lateinit var binding: FragmentAlertBinding
    private lateinit var bindingAlertLayout: AlertDialogLayoutBinding
    private lateinit var alertViewModel: AlertViewModel
    private lateinit var alertRecyclerAdapter: AlertRecyclerAdapter


    private  val alarmManager : AlarmManager by lazy {
        requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = AlertViewModelFactory(
            Repo.getInstance(
                RemoteDataSource, LocationClient.getInstance(
                    LocationServices.getFusedLocationProviderClient(view.context),
                ),
                LocalDataSource.getInstance(requireContext()),
                SettingSharedPref.getInstance(requireContext())
            )
        )

        alertViewModel = ViewModelProvider(this, factory)[AlertViewModel::class.java]

        alertViewModel.getAllAlarms()

        alertRecyclerAdapter = AlertRecyclerAdapter()
        binding.rvAlerts.adapter = alertRecyclerAdapter
        deleteBySwipe(view)

        lifecycleScope.launch {
            alertViewModel.alarmsStateFlow.collectLatest {
                alertRecyclerAdapter.submitList(it)
            }
        }

        binding.fabAddAlert.setOnClickListener {
            if (alertViewModel.isNotificationEnabled()) {
                if (PermissionUtility.notificationPermission(requireContext())) {
                    showTimeDialog()
                } else {
                    showSettingDialog()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "you need to enable notification first.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnEnableNotification.setOnClickListener {
            requestNotificationPermission()
        }

        binding.btnEnableAlert.setOnClickListener {
            requestOverlayPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        tellUserAboutPermissions()
    }

    private fun showTimeDialog() {
        val currentTimeInMillis = System.currentTimeMillis()

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        bindingAlertLayout = AlertDialogLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(bindingAlertLayout.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingAlertLayout.tvFromDateDialog.text =
            Functions.formatLongToAnyString(currentTimeInMillis, "dd MMM yyyy")
        bindingAlertLayout.tvFromTimeDialog.text =
            Functions.formatLongToAnyString(currentTimeInMillis + 60 * 1000, "hh:mm a")

        bindingAlertLayout.cvFrom.setOnClickListener {
            showDatePicker()
        }

        bindingAlertLayout.radioGroupAlertDialog.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == bindingAlertLayout.radioAlert.id && !Settings.canDrawOverlays(
                    requireContext()
                )
            ) {
                requestOverlayPermission()
                dialog.dismiss()
            }
        }

        bindingAlertLayout.btnSaveDialog.setOnClickListener {

            val kindId = bindingAlertLayout.radioGroupAlertDialog.checkedRadioButtonId
            var kind: String = Constants.NOTIFICATION
            if (kindId == bindingAlertLayout.radioAlert.id) {
                kind = Constants.ALERT
            }

            val time = Functions.formatFromStringToLong(
                bindingAlertLayout.tvFromDateDialog.text.toString(),
                bindingAlertLayout.tvFromTimeDialog.text.toString()
            )

            val alarmItem = AlarmItem(time, kind)
            if (time > currentTimeInMillis) {
                alertViewModel.insertAlarm(alarmItem)


                setUpTheAlarm(alarmItem)

                dialog.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "please select a time in the future",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
    }

    private fun setUpTheAlarm(alert: AlarmItem) {
        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alert.time,
                getPendingIntent(alert)
            )
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Failed to set alarm: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getPendingIntent(alert: AlarmItem): PendingIntent {
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            action = if (alert.kind == Constants.NOTIFICATION) {
                Constants.ALERT_ACTION_NOTIFICATION
            } else {
                Constants.ALERT_ACTION_ALARM
            }
            putExtra(Constants.ALERT_KEY, alert)
        }

        return PendingIntent.getBroadcast(
            requireContext(),
            alert.time.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }




    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            com.google.android.material.R.style.MaterialAlertDialog_Material3
        )
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:com.example.airytics")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:com.example.airytics")
        startActivity(intent)
    }

    private fun requestNotificationPermission() {
        val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:com.example.airytics")
        startActivity(intent)
    }


    private fun deleteBySwipe(view: View) {
        val itemTouchHelperCallBack = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val alarmItem = alertRecyclerAdapter.currentList[position]

                cancelAlarm(alarmItem)
                alertViewModel.deleteAlarm(alarmItem)

                val mediaPlayer = MediaPlayer.create(context, R.raw.delete)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { mp -> mp.release() }


                Snackbar.make(view, "Alarm deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {

                        alertViewModel.insertAlarm(alarmItem)
                    }
                    show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallBack)
        itemTouchHelper.attachToRecyclerView(binding.rvAlerts)
    }

    private fun cancelAlarm(alarmItem: AlarmItem) {
        val pendingIntent = getPendingIntent(alarmItem)
        alarmManager.cancel(pendingIntent)
    }


    private fun showDatePicker() {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .setTheme(R.style.ThemeOverlay_App_DatePicker)
                .setTitleText("Select date")
                .build()

        datePicker.show(parentFragmentManager, "date")

        datePicker.addOnPositiveButtonClickListener { date ->
            bindingAlertLayout.tvFromDateDialog.text =
                Functions.formatLongToAnyString(date, "dd MMM yyyy")
            showTimePicker()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val timePicker =
            MaterialTimePicker.Builder()
                .setInputMode(INPUT_MODE_CLOCK)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(currentHour)
                .setMinute(currentMinute + 1)
                .setTitleText("Select Appointment time")
                .build()

        timePicker.show(parentFragmentManager, "time")
        timePicker.addOnPositiveButtonClickListener {

            bindingAlertLayout.tvFromTimeDialog.text =
                Functions.formatHourMinuteToString(timePicker.hour, timePicker.minute)

        }

        timePicker.addOnCancelListener {

            bindingAlertLayout.tvFromTimeDialog.text =
                Functions.formatHourMinuteToString(timePicker.hour, timePicker.minute)

        }
    }

    private fun tellUserAboutPermissions() {
        if (PermissionUtility.notificationPermission(requireContext())) {
            binding.tvAlertUserNotification.visibility = View.GONE
            binding.btnEnableNotification.visibility = View.GONE
        } else {
            binding.tvAlertUserNotification.visibility = View.VISIBLE
            binding.btnEnableNotification.visibility = View.VISIBLE
        }

        if (Settings.canDrawOverlays(requireContext())) {
            binding.tvAlertUserAlert.visibility = View.GONE
            binding.btnEnableAlert.visibility = View.GONE
        } else {
            binding.tvAlertUserAlert.visibility = View.VISIBLE
            binding.btnEnableAlert.visibility = View.VISIBLE
        }
    }






}