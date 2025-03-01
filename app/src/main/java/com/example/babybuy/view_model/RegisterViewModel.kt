package com.example.babybuy.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.babybuy.repository.UserRespository


class RegisterViewModel(private val userRepository : UserRespository) : ViewModel() {

    private val _registerResponse = MutableLiveData<Pair<Boolean, String>>()
    val registerResponse: LiveData<Pair<Boolean, String>> = _registerResponse
    fun createUser(email: String, password: String, name: String) {
        userRepository.createUser(email, password, name)
        { success, message ->
            _registerResponse.postValue(Pair(success, message))
        }
    }
}