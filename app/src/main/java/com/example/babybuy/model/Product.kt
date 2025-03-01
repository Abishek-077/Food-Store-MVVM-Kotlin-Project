package com.example.babybuy.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    var id: String = "",
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val image: String = "",
    val location: String? = null,
    var isPurchased: Boolean = false
) : Parcelable


data class FirestoreProduct(
    var id: String = "",
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val image: String = "",
    val location: String? = null,
    val purchased: Boolean = false
)
