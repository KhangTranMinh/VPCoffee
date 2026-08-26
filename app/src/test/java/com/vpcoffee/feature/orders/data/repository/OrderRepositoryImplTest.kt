package com.vpcoffee.feature.orders.data.repository

import com.vpcoffee.core.data.local.AppDatabase
import com.vpcoffee.feature.orders.data.local.OrderDao
import com.vpcoffee.feature.orders.data.local.OrderEntity
import com.vpcoffee.feature.orders.data.local.OrderItemEntity
import com.vpcoffee.feature.orders.data.local.OrderWithItems
import com.vpcoffee.feature.orders.domain.model.Order
import com.vpcoffee.feature.orders.domain.model.OrderItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var orderDao: OrderDao
    private lateinit var repository: OrderRepositoryImpl

    @Before
    fun setup() {
        database = mockk(relaxed = true)
        orderDao = mockk()
        repository = OrderRepositoryImpl(database, orderDao)
    }

    @Test
    fun `observeOrders maps OrderWithItems to domain Order`() = runTest {
        val orderWithItems = listOf(
            OrderWithItems(
                order = OrderEntity("order-1", 1000L, null),
                items = listOf(
                    OrderItemEntity("order-1", "drink-1", "Coffee", 25000, 2),
                    OrderItemEntity("order-1", "drink-2", "Tea", 20000, 1),
                ),
            )
        )
        every { orderDao.observeAll() } returns flowOf(orderWithItems)

        val result = repository.observeOrders().first()

        assertEquals(1, result.size)
        assertEquals("order-1", result[0].id)
        assertEquals(1000L, result[0].createdAt)
        assertEquals(2, result[0].items.size)
        assertEquals("Coffee", result[0].items[0].drinkName)
        assertEquals(25000, result[0].items[0].unitPrice)
        assertEquals(2, result[0].items[0].quantity)
    }

    @Test
    fun `observeOrders preserves sentAt field`() = runTest {
        val orderWithItems = listOf(
            OrderWithItems(
                order = OrderEntity("order-1", 1000L, 2000L),
                items = emptyList(),
            )
        )
        every { orderDao.observeAll() } returns flowOf(orderWithItems)

        val result = repository.observeOrders().first()

        assertEquals(2000L, result[0].sentAt)
    }

    @Test
    fun `observeOrders returns empty list when dao returns empty`() = runTest {
        every { orderDao.observeAll() } returns flowOf(emptyList())

        val result = repository.observeOrders().first()

        assertEquals(0, result.size)
    }

    @Test
    fun `markOrdersAsSent calls dao markAsSent`() = runTest {
        coEvery { orderDao.markAsSent(any(), any()) } returns Unit

        repository.markOrdersAsSent(listOf("order-1", "order-2"))

        coVerify {
            orderDao.markAsSent(
                match { it == listOf("order-1", "order-2") },
                any()
            )
        }
    }
}
