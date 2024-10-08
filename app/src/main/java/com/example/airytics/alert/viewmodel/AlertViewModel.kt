package com.example.airytics.alert.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airytics.model.RepoInterface
import com.example.airytics.network.ApiState
import com.example.airytics.model.AlarmItem
import com.example.airytics.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertViewModel(private val repo: RepoInterface) :
    ViewModel() {

    private val _alarmsMutableStateFlow: MutableStateFlow<List<AlarmItem>> = MutableStateFlow(
        emptyList()
    )
    val alarmsStateFlow: StateFlow<List<AlarmItem>> get() = _alarmsMutableStateFlow

    private val _weatherResponseMutableStateFlow: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Loading)
    val weatherResponseStateFlow: StateFlow<ApiState> get() = _weatherResponseMutableStateFlow


    fun insertAlarm(alarmItem: AlarmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.insertAlarm(alarmItem)
        }
    }

    fun deleteAlarm(alarmItem: AlarmItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAlarm(alarmItem)
        }
    }

    fun getAllAlarms() {
        viewModelScope.launch {
            repo.getAllAlarms().collectLatest {
                _alarmsMutableStateFlow.value = it
            }
        }
    }


    fun readStringFromSettingSP(key: String): String {
        return repo.readStringFromSettingSP(key)
    }

    fun isNotificationEnabled() =
        readStringFromSettingSP(Constants.NOTIFICATION) != Constants.DISABLE

}