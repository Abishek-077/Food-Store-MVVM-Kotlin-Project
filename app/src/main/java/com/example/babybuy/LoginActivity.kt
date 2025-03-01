package com.example.babybuy

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.babybuy.model.User
import com.example.babybuy.repository.UserRespositoryImpl
import com.example.babybuy.view_model.LoginViewModel
import com.example.babybuy.view_model.LoginViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), SensorEventListener  {
    private val tag= "LoginActivity"
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var loginButton: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var viewModel: LoginViewModel
    private var shakeCount = 0
    private var lastShakeTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        Log.i(tag, "onCreate Called....")

        val signUpNowText = findViewById<TextView>(R.id.signup_now_text)
        signUpNowText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        val userRepository = UserRespositoryImpl(auth = FirebaseAuth.getInstance())
        val factory = LoginViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        viewModel.loginStatus.observe(this) { status ->
            val (success, message) = status
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (success) {
                val user = FirebaseAuth.getInstance().currentUser
                val userId = user?.uid
                val intent = Intent(this@LoginActivity, Dashboard::class.java).apply {
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)
            }
        }

        loginButton = findViewById(R.id.btn_login)
                etEmail= findViewById(R.id.et_email)
                etPassword= findViewById(R.id.et_password)
                loginButton.text = "Log in"

                loginButton.setOnClickListener{
                    val email = etEmail.text.toString().trim()
                    val password = etPassword.text.toString().trim()

                    if (email.isBlank() ||
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        Toast.makeText(
                            this,
                            "Enter a valid email",
                            Toast.LENGTH_SHORT).show()
                        etEmail.error = "Email is incorrect"
                    }
                    else if(password.isBlank() ||
                        !isValidPassword(password)){

                        Toast.makeText(
                            this,
                            "Enter a valid password",
                            Toast.LENGTH_SHORT).show()
                        etPassword.error = "Password is incorrect"
                    }
                    else{
                        viewModel.loginUser(email, password)
                    }
                }

                // Initialize the sensor manager and accelerometer
                sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            }

    private fun isValidPassword(password: String): Boolean {
                // Minimum 8 characters, at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special symbol
                val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
                return password.matches(passwordPattern.toRegex())
            }

    override fun onStart() {
                super.onStart()
                Log.i(tag, "onStart Called....")
            }

    override fun onResume() {
                accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
                super.onResume()
            }

            override fun onPause() {
                sensorManager.unregisterListener(this)
                super.onPause()
            }

            override fun onStop() {
                super.onStop()
                Log.i(tag, "onStop Called....")
            }

            override fun onDestroy() {
                super.onDestroy()
                Log.i(tag, "onDestroy Called....")
            }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val currentTime = System.currentTimeMillis()
                val diffTime = currentTime - lastShakeTime

                if (diffTime > 1000) {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())
                    if (acceleration > 12) { // Shake threshold
                        shakeCount++
                        lastShakeTime = currentTime

                        if (shakeCount == 2) { // Trigger login on second shake
                            shakeCount = 0 // Reset shake count
                            triggerLogin()
                        }
                    }
                }
            }
        }
    }

    private fun triggerLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.loginUser(email, password)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


            private fun showToast(message: String){
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }


            private fun onSuccessfulLogin(userInDb: User){
                // Writing to shared preferences
                val sharedPreferences= this.getSharedPreferences(
                    AppConstant.FILE_SHARED_PREF_LOGIN,
                    Context.MODE_PRIVATE
                )
                val sharedPrefEditor= sharedPreferences.edit()
                sharedPrefEditor.putBoolean(
                    AppConstant.KEY_IS_LOGGED_IN,
                    true
                )

                sharedPrefEditor.apply()


                val intent = Intent(
                    this,
                    Dashboard::class.java
                )

                intent.putExtra(AppConstant.KEY_LOGIN_DATA,userInDb)
                startActivity(intent)
                finish()
            }

        }