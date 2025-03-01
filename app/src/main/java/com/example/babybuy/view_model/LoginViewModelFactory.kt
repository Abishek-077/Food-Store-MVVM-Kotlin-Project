package com.example.babybuy.view_model
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.babybuy.repository.UserRespository

class   LoginViewModelFactory(private val userRepository: UserRespository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}