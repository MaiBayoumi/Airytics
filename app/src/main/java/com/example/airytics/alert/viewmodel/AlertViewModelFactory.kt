package com.example.airytics.alert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.airytics.model.RepoInterface

@Suppress("UNCHECKED_CAST")
class AlertViewModelFactory(private val repo: RepoInterface) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
            return AlertViewModel(repo) as T
        }
        return super.create(modelClass, extras)
    }
}