package com.example.airytics.alert.service

import android.content.Context
import com.example.airytics.model.AlarmItem

interface AlarmSchedulerInterface {
    fun createAlarm(item: AlarmItem, context: Context)
    fun cancelAlarm(item: AlarmItem, context: Context)
}