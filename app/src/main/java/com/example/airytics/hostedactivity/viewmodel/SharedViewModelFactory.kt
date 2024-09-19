package com.example.airytics.hostedactivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.airytics.model.RepoInterface

class SharedViewModelFactory(
    private val repo: RepoInterface
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            return SharedViewModel(repo) as T
        }
        return super.create(modelClass, extras)
    }
}