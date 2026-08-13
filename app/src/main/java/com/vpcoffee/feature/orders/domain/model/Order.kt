package com.vpcoffee.feature.orders.domain.model

data class Order(
    val id: Long = 0,
    val createdAt: Long,
    val items: List<OrderItem>,
) {
    val total: Long get() = items.sumOf { it.unitPrice * it.quantity }
}

data class OrderItem(
    val drinkId: Long,
    val drinkName: String,
    val unitPrice: Long,
    val quantity: Int,
)
