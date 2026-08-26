package com.vpcoffee.feature.orders.presentation

import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import com.vpcoffee.feature.orders.domain.model.Order
import com.vpcoffee.feature.orders.domain.model.OrderItem
import com.vpcoffee.feature.orders.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var orderRepository: OrderRepository
    private lateinit var drinkRepository: DrinkRepository
    private lateinit var viewModel: ReportsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        orderRepository = mockk()
        drinkRepository = mockk()
        every { orderRepository.observeOrders() } returns flowOf(emptyList())
        every { drinkRepository.observeDrinks() } returns flowOf(emptyList())
        viewModel = ReportsViewModel(orderRepository, drinkRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `orders emits empty list initially`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.orders.value.isEmpty())
    }

    @Test
    fun `markOrdersAsSent calls repository`() = runTest {
        coEvery { orderRepository.markOrdersAsSent(any()) } returns Unit

        orderRepository.markOrdersAsSent(listOf("order-1", "order-2"))

        coVerify { orderRepository.markOrdersAsSent(listOf("order-1", "order-2")) }
    }
}
