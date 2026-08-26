package com.vpcoffee.feature.orders.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vpcoffee.core.data.local.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var orderDao: OrderDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        orderDao = database.orderDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `observeAll returns empty list when no orders`() = runTest {
        val result = orderDao.observeAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `upsertOrder inserts order`() = runTest {
        val order = OrderEntity("order-1", 1000L)
        orderDao.upsertOrder(order)

        val result = orderDao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("order-1", result[0].order.id)
        assertEquals(1000L, result[0].order.createdAt)
    }

    @Test
    fun `upsertOrder updates existing order`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))
        orderDao.upsertOrder(OrderEntity("order-1", 2000L))

        val result = orderDao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals(2000L, result[0].order.createdAt)
    }

    @Test
    fun `upsertItems inserts items for order`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))
        orderDao.upsertItems(listOf(
            OrderItemEntity("order-1", "drink-1", "Coffee", 25000, 2),
            OrderItemEntity("order-1", "drink-2", "Tea", 20000, 1),
        ))

        val result = orderDao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals(2, result[0].items.size)
    }

    @Test
    fun `observeAll returns orders with items via Relation`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))
        orderDao.upsertItems(listOf(
            OrderItemEntity("order-1", "drink-1", "Coffee", 25000, 2),
        ))

        val result = orderDao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("order-1", result[0].order.id)
        assertEquals(1, result[0].items.size)
        assertEquals("Coffee", result[0].items[0].drinkName)
        assertEquals(25000, result[0].items[0].unitPrice)
        assertEquals(2, result[0].items[0].quantity)
    }

    @Test
    fun `observeAll orders by createdAt DESC`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))
        orderDao.upsertOrder(OrderEntity("order-2", 3000L))
        orderDao.upsertOrder(OrderEntity("order-3", 2000L))

        val result = orderDao.observeAll().first()
        assertEquals(3, result.size)
        assertEquals("order-2", result[0].order.id)
        assertEquals("order-3", result[1].order.id)
        assertEquals("order-1", result[2].order.id)
    }

    @Test
    fun `markAsSent updates sentAt for specified orders`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))
        orderDao.upsertOrder(OrderEntity("order-2", 2000L))
        orderDao.markAsSent(listOf("order-1"), 5000L)

        val result = orderDao.observeAll().first()
        val order1 = result.find { it.order.id == "order-1" }
        val order2 = result.find { it.order.id == "order-2" }

        assertEquals(5000L, order1?.order?.sentAt)
        assertNull(order2?.order?.sentAt)
    }

    @Test
    fun `sentAt is null by default`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))

        val result = orderDao.observeAll().first()
        assertNull(result[0].order.sentAt)
    }

    @Test
    fun `markAsSent updates multiple orders`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))
        orderDao.upsertOrder(OrderEntity("order-2", 2000L))
        orderDao.upsertOrder(OrderEntity("order-3", 3000L))
        orderDao.markAsSent(listOf("order-1", "order-3"), 5000L)

        val result = orderDao.observeAll().first()
        val order1 = result.find { it.order.id == "order-1" }
        val order2 = result.find { it.order.id == "order-2" }
        val order3 = result.find { it.order.id == "order-3" }

        assertEquals(5000L, order1?.order?.sentAt)
        assertNull(order2?.order?.sentAt)
        assertEquals(5000L, order3?.order?.sentAt)
    }

    @Test
    fun `upsertItems updates existing items`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))
        orderDao.upsertItems(listOf(
            OrderItemEntity("order-1", "drink-1", "Coffee", 25000, 2),
        ))
        orderDao.upsertItems(listOf(
            OrderItemEntity("order-1", "drink-1", "Coffee", 25000, 5),
        ))

        val result = orderDao.observeAll().first()
        assertEquals(1, result[0].items.size)
        assertEquals(5, result[0].items[0].quantity)
    }

    @Test
    fun `order with multiple items`() = runTest {
        orderDao.upsertOrder(OrderEntity("order-1", 1000L))
        orderDao.upsertItems(listOf(
            OrderItemEntity("order-1", "drink-1", "Coffee", 25000, 2),
            OrderItemEntity("order-1", "drink-2", "Tea", 20000, 1),
            OrderItemEntity("order-1", "drink-3", "Americano", 22000, 3),
        ))

        val result = orderDao.observeAll().first()
        assertEquals(3, result[0].items.size)
    }
}
