package com.vpcoffee.feature.catalog.domain.model

data class Drink(
    val id: String,
    val name: String,
    val price: Long,
    val imageUri: String? = null,
)
