package com.example.babybuy.repository;

import android.util.Log
import com.example.babybuy.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class UserRespositoryImpl(var auth: FirebaseAuth) : UserRespository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun createUser(
        email: String,
        password: String,
        name: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d("Signup", "User: $user");
                val newUser = UserModel(name, email)
                user?.let {
                    db.collection("users").document(it.uid).set(newUser)
                        .addOnSuccessListener {
                            callback(true, "User Created")
                        }
                        .addOnFailureListener { exception ->
                            exception.localizedMessage?.let { it1 -> callback(false, it1) }
                        }
                }

            } else {
                callback(false, "Failed to create an user ${task.exception?.localizedMessage}")
            }

        }
    }

    override fun loginUser(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "login successfull")
                } else {
                    callback(false, "Login failed: ${task.exception?.message}")
                }
            }
    }

    override fun signInwithGoogle(idToken: String, callback: (Boolean, String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.exception?.let { callback(task.isSuccessful, it.localizedMessage) }
                }
            }


    }
    override fun signOut() {
        auth.signOut()
    }
}