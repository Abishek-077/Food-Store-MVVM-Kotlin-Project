package com.example.babybuy.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.babybuy.repository.UserRespository

class RegisterViewModelFactory(private val userRespository: UserRespository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(userRespository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}