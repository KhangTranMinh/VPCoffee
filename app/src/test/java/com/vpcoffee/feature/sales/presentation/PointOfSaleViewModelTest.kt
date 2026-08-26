package com.vpcoffee.feature.sales.presentation

import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import com.vpcoffee.feature.orders.domain.model.Order
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
class PointOfSaleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var drinkRepository: DrinkRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var viewModel: PointOfSaleViewModel

    private val coffee = Drink("1", "Coffee", 25000)
    private val tea = Drink("2", "Tea", 20000)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        drinkRepository = mockk()
        orderRepository = mockk()
        every { drinkRepository.observeDrinks() } returns flowOf(emptyList())
        viewModel = PointOfSaleViewModel(drinkRepository, orderRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cart is empty initially`() = runTest {
        assertTrue(viewModel.cart.value.isEmpty())
    }

    @Test
    fun `addDrink adds new item to cart`() = runTest {
        viewModel.addDrink(coffee)
        assertEquals(1, viewModel.cart.value.size)
        assertEquals("1", viewModel.cart.value[0].drinkId)
        assertEquals(1, viewModel.cart.value[0].quantity)
    }

    @Test
    fun `addDrink increments quantity for existing item`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.addDrink(coffee)
        assertEquals(1, viewModel.cart.value.size)
        assertEquals(2, viewModel.cart.value[0].quantity)
    }

    @Test
    fun `addDrink adds separate items for different drinks`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.addDrink(tea)
        assertEquals(2, viewModel.cart.value.size)
    }

    @Test
    fun `changeQuantity updates item quantity`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.changeQuantity("1", 5)
        assertEquals(5, viewModel.cart.value[0].quantity)
    }

    @Test
    fun `changeQuantity removes item when quantity is 0`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.changeQuantity("1", 0)
        assertTrue(viewModel.cart.value.isEmpty())
    }

    @Test
    fun `changeQuantity does not affect other items`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.addDrink(tea)
        viewModel.changeQuantity("1", 5)
        assertEquals(5, viewModel.cart.value[0].quantity)
        assertEquals(1, viewModel.cart.value[1].quantity)
    }

    @Test
    fun `completeOrder does nothing when cart is empty`() = runTest {
        var completed = false
        viewModel.completeOrder { completed = true }
        advanceUntilIdle()
        coVerify(exactly = 0) { orderRepository.saveOrder(any()) }
        assertTrue(!completed)
    }

    @Test
    fun `completeOrder saves order and clears cart`() = runTest {
        coEvery { orderRepository.saveOrder(any()) } returns "order-1"
        viewModel.addDrink(coffee)
        var completed = false
        viewModel.completeOrder { completed = true }
        advanceUntilIdle()
        coVerify(exactly = 1) { orderRepository.saveOrder(any()) }
        assertTrue(viewModel.cart.value.isEmpty())
        assertTrue(completed)
    }
}
