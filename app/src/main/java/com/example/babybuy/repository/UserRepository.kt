package com.example.babybuy.repository
import androidx.activity.result.ActivityResultCallback

interface UserRespository {
    fun createUser(
        email: String,
        password: String,
        name: String,
        callback: (Boolean, String) -> Unit
    )
    fun loginUser(email: String,password: String ,callback : (Boolean ,String) -> Unit)
    fun signInwithGoogle(idToken:String, callback: (Boolean, String) -> Unit)
    fun signOut()
}
