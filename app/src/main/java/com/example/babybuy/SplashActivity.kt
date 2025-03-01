package com.example.babybuy

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        Handler().postDelayed(
            {
                //Code to be executed after the below delay time

                //Read from shared preferences
                val sharedPreferences = this.getSharedPreferences(
                    AppConstant.FILE_SHARED_PREF_LOGIN,
                    Context.MODE_PRIVATE
                )
                val defaultLoginState= false
                val isAlreadyLoggedIn= sharedPreferences.getBoolean(
                    AppConstant.KEY_IS_LOGGED_IN,
                    defaultLoginState
                )
                val intent: Intent
                if (isAlreadyLoggedIn){
                    intent= Intent(
                        this,
                        Dashboard::class.java)
                }
                else{
                    intent= Intent(
                        this,
                        HomeActivity::class.java)
                }
                startActivity(intent)
                finish()
            }, 3500) // 3500 milliseconds (2 seconds) delay
    }
}
