package com.example.airytics.alert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.airytics.alert.service.AlarmScheduler
import com.example.airytics.model.RepoInterface

class AlertViewModelFactory(private val repo: RepoInterface, private val alarmScheduler: AlarmScheduler) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
            return AlertViewModel(repo, alarmScheduler) as T
        }
        return super.create(modelClass, extras)
    }
}