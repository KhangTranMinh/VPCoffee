package com.vpcoffee.domain.model

data class Drink(
    val id: Long = 0,
    val name: String,
    val price: Long,
    val imageUri: String? = null,
)
