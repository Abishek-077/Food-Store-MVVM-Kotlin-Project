package com.example.babybuy.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.babybuy.repository.UserRespository

class LoginViewModel(private  val userRespository: UserRespository) : ViewModel() {
    private val _loginStatus = MutableLiveData<Pair<Boolean,String>>()
    val loginStatus: LiveData<Pair<Boolean, String>> = _loginStatus

    fun loginUser(email:String,password:String){
        userRespository.loginUser(email,password){
            success, message ->
            _loginStatus.value = Pair(success,message)
        }
    }

    fun signUpWithGoogle(idToken: String, callback: (Boolean, String) -> Unit){
        userRespository.signInwithGoogle(idToken , callback)
    }
    fun signOut(){
        userRespository.signOut()
    }

}