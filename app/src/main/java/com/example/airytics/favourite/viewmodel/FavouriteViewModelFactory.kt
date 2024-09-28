package com.example.airytics.favourite.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.airytics.model.RepoInterface

@Suppress("UNCHECKED_CAST")
class FavouriteViewModelFactory(private val repo: RepoInterface): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(FavouriteViewModel::class.java)) {
            return FavouriteViewModel(repo) as T
        }
        return super.create(modelClass, extras)
    }
}