package com.example.shoppinglist.data

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val quantity: Int,
    val isBought: Boolean = false
)