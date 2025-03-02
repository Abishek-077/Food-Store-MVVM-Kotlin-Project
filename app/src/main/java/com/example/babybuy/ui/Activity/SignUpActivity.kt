package com.example.babybuy.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.babybuy.R
import com.example.babybuy.databinding.ActivitySignupBinding
import com.example.babybuy.repository.UserRespositoryImpl
import com.example.babybuy.view_model.RegisterViewModel
import com.example.babybuy.view_model.RegisterViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySignupBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.ibBack.setOnClickListener{
            val intent= Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val userRepository = UserRespositoryImpl(auth = FirebaseAuth.getInstance())
        val factory = RegisterViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]

        viewModel.registerResponse.observe(this) { response ->
            val (success, message) = response
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (success) {
                val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        viewBinding.btnSignup.setOnClickListener{
            //TODO Validation
            val inputFullName = viewBinding.etFullname.text.toString()
            val inputEmail = viewBinding.etEmail.text.toString()
            val inputPassword = viewBinding.etPassword.text.toString()
            val confirmPassword = viewBinding.etConfirmPassword.text.toString()

            // Check if fields are empty
            if (inputFullName.isEmpty() || inputEmail.isEmpty() || inputPassword.isEmpty() || confirmPassword.isEmpty()) {
                showToast("Please fill in all the fields.")
            }

            // Check if full name has at least two words
            val fullNameWords = inputFullName.split(" ")
            if (fullNameWords.size < 2) {
                val etFullName = findViewById<EditText>(R.id.et_fullname)
                etFullName.error = "Enter the full name"
                return@setOnClickListener
            }

            // Check email structure
            if (!isValidEmail(inputEmail)) {
                val etEmail = findViewById<EditText>(R.id.et_email)
                etEmail.error = "Invalid email format"
                return@setOnClickListener

            }

            // Check password format
            if (!isValidPassword(inputPassword)) {
                val etPassword = findViewById<EditText>(R.id.et_password)
                etPassword.error = "Weak Password"
                return@setOnClickListener
            }

            // Check if password matches with confirm password
            if (inputPassword != confirmPassword) {
                val etConfirmPassword = findViewById<EditText>(R.id.et_confirmPassword)
                etConfirmPassword.error = "Password doesn't match"
                return@setOnClickListener
            }


            viewModel.createUser(inputEmail, inputPassword, inputFullName)
        }
    }
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z]+\\.+[a-zA-Z]+")
        return emailPattern.matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        // Minimum 8 characters, at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special symbol
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    private fun clearInputFieldsData(){
        viewBinding.etFullname.text?.clear()
        viewBinding.etEmail.text?.clear()
        viewBinding.etPassword.text?.clear()
        viewBinding.etConfirmPassword.text?.clear()
    }

    private fun showToast(message: String){
        Toast.makeText(
            this, message, Toast.LENGTH_LONG).show()

    }
}