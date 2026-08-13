package com.vpcoffee.feature.orders.domain.model

data class Order(
    val id: String,
    val createdAt: Long,
    val items: List<OrderItem>,
) {
    val total: Long get() = items.sumOf { it.unitPrice * it.quantity }
}

data class OrderItem(
    val drinkId: String,
    val drinkName: String,
    val unitPrice: Long,
    val quantity: Int,
)
