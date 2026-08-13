package com.vpcoffee.feature.orders.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderTest {
    @Test
    fun total_sums_each_item_price_and_quantity() {
        val order = Order(
            id = "order-1",
            createdAt = 0,
            items = listOf(
                OrderItem(drinkId = "drink-1", drinkName = "Coffee", unitPrice = 25_000, quantity = 2),
                OrderItem(drinkId = "drink-2", drinkName = "Tea", unitPrice = 18_000, quantity = 1),
            ),
        )

        assertEquals(68_000, order.total)
    }

    @Test
    fun total_is_zero_for_an_empty_order() {
        assertEquals(0, Order(id = "order-1", createdAt = 0, items = emptyList()).total)
    }
}
