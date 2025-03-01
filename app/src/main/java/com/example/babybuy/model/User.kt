package com.example.babybuy.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id: Int = 0,
    val fullName: String,
    val email: String,
    val password: String
): Parcelable

