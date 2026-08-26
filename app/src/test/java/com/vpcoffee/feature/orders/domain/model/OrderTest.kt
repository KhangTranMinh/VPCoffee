package com.vpcoffee.feature.orders.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun total_with_single_item() {
        val order = Order(
            id = "order-1",
            createdAt = 0,
            items = listOf(
                OrderItem(drinkId = "drink-1", drinkName = "Coffee", unitPrice = 25_000, quantity = 1),
            ),
        )
        assertEquals(25_000, order.total)
    }

    @Test
    fun total_with_zero_price_item() {
        val order = Order(
            id = "order-1",
            createdAt = 0,
            items = listOf(
                OrderItem(drinkId = "drink-1", drinkName = "Free Sample", unitPrice = 0, quantity = 5),
            ),
        )
        assertEquals(0, order.total)
    }

    @Test
    fun total_with_zero_quantity_item() {
        val order = Order(
            id = "order-1",
            createdAt = 0,
            items = listOf(
                OrderItem(drinkId = "drink-1", drinkName = "Coffee", unitPrice = 25_000, quantity = 0),
            ),
        )
        assertEquals(0, order.total)
    }

    @Test
    fun total_with_large_quantities() {
        val order = Order(
            id = "order-1",
            createdAt = 0,
            items = listOf(
                OrderItem(drinkId = "drink-1", drinkName = "Coffee", unitPrice = 25_000, quantity = 1000),
            ),
        )
        assertEquals(25_000_000, order.total)
    }

    @Test
    fun total_with_large_price() {
        val order = Order(
            id = "order-1",
            createdAt = 0,
            items = listOf(
                OrderItem(drinkId = "drink-1", drinkName = "Premium Coffee", unitPrice = 999_999, quantity = 1),
            ),
        )
        assertEquals(999_999, order.total)
    }

    @Test
    fun total_with_multiple_items_same_drink() {
        val order = Order(
            id = "order-1",
            createdAt = 0,
            items = listOf(
                OrderItem(drinkId = "drink-1", drinkName = "Coffee", unitPrice = 25_000, quantity = 2),
                OrderItem(drinkId = "drink-1", drinkName = "Coffee", unitPrice = 25_000, quantity = 3),
            ),
        )
        // Both items are counted separately
        assertEquals(125_000, order.total)
    }

    @Test
    fun isSent_false_when_sentAt_is_null() {
        val order = Order(id = "order-1", createdAt = 0, items = emptyList(), sentAt = null)
        assertFalse(order.isSent)
    }

    @Test
    fun isSent_true_when_sentAt_is_set() {
        val order = Order(id = "order-1", createdAt = 0, items = emptyList(), sentAt = 1000L)
        assertTrue(order.isSent)
    }

    @Test
    fun isSent_true_when_sentAt_is_zero() {
        val order = Order(id = "order-1", createdAt = 0, items = emptyList(), sentAt = 0L)
        assertTrue(order.isSent)
    }

    @Test
    fun order_preserves_id() {
        val order = Order(id = "custom-id", createdAt = 12345L, items = emptyList())
        assertEquals("custom-id", order.id)
        assertEquals(12345L, order.createdAt)
    }

    @Test
    fun orderItem_preserves_all_fields() {
        val item = OrderItem(
            drinkId = "drink-1",
            drinkName = "Coffee",
            unitPrice = 25_000,
            quantity = 2,
        )
        assertEquals("drink-1", item.drinkId)
        assertEquals("Coffee", item.drinkName)
        assertEquals(25_000, item.unitPrice)
        assertEquals(2, item.quantity)
    }
}
