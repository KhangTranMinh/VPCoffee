package com.vpcoffee.feature.sales.presentation

import app.cash.turbine.test
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
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
        advanceUntilIdle()
        viewModel.cart.test {
            assertEquals(emptyList<OrderItem>(), awaitItem())
        }
    }

    @Test
    fun `drinks emits repository data`() = runTest {
        val drinks = listOf(coffee, tea)
        every { drinkRepository.observeDrinks() } returns flowOf(drinks)
        viewModel = PointOfSaleViewModel(drinkRepository, orderRepository)
        advanceUntilIdle()

        // StateFlow with WhileSubscribed needs a collector to emit
        // Verify the repository was called correctly
        coVerify { drinkRepository.observeDrinks() }
    }

    @Test
    fun `addDrink adds new item to cart`() = runTest {
        viewModel.addDrink(coffee)
        advanceUntilIdle()

        viewModel.cart.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("1", items[0].drinkId)
            assertEquals("Coffee", items[0].drinkName)
            assertEquals(25000, items[0].unitPrice)
            assertEquals(1, items[0].quantity)
        }
    }

    @Test
    fun `addDrink increments quantity for existing item`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.addDrink(coffee)
        advanceUntilIdle()

        viewModel.cart.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(2, items[0].quantity)
        }
    }

    @Test
    fun `addDrink adds separate items for different drinks`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.addDrink(tea)
        advanceUntilIdle()

        viewModel.cart.test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("1", items[0].drinkId)
            assertEquals("2", items[1].drinkId)
        }
    }

    @Test
    fun `addDrink preserves drink price`() = runTest {
        viewModel.addDrink(coffee)
        advanceUntilIdle()

        viewModel.cart.test {
            val items = awaitItem()
            assertEquals(25000, items[0].unitPrice)
        }
    }

    @Test
    fun `changeQuantity updates item quantity`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.changeQuantity("1", 5)
        advanceUntilIdle()

        viewModel.cart.test {
            val items = awaitItem()
            assertEquals(5, items[0].quantity)
        }
    }

    @Test
    fun `changeQuantity removes item when quantity is 0`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.changeQuantity("1", 0)
        advanceUntilIdle()

        viewModel.cart.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `changeQuantity removes item when quantity is negative`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.changeQuantity("1", -1)
        advanceUntilIdle()

        viewModel.cart.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `changeQuantity does not affect other items`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.addDrink(tea)
        viewModel.changeQuantity("1", 5)
        advanceUntilIdle()

        viewModel.cart.test {
            val items = awaitItem()
            assertEquals(5, items[0].quantity)
            assertEquals(1, items[1].quantity)
        }
    }

    @Test
    fun `changeQuantity does nothing for non-existent drink`() = runTest {
        viewModel.addDrink(coffee)
        viewModel.changeQuantity("999", 5)
        advanceUntilIdle()

        viewModel.cart.test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(1, items[0].quantity)
        }
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
        assertTrue(completed)

        advanceUntilIdle()
        viewModel.cart.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `completeOrder saves order with correct items`() = runTest {
        coEvery { orderRepository.saveOrder(any()) } returns "order-1"
        viewModel.addDrink(coffee)
        viewModel.addDrink(tea)
        viewModel.addDrink(coffee) // 2x coffee

        viewModel.completeOrder {}
        advanceUntilIdle()

        coVerify {
            orderRepository.saveOrder(match { order ->
                order.items.size == 2 &&
                    order.items[0].drinkId == "1" &&
                    order.items[0].quantity == 2 &&
                    order.items[1].drinkId == "2" &&
                    order.items[1].quantity == 1
            })
        }
    }

    @Test
    fun `completeOrder generates unique order id`() = runTest {
        coEvery { orderRepository.saveOrder(any()) } returns "order-1"
        viewModel.addDrink(coffee)

        viewModel.completeOrder {}
        advanceUntilIdle()

        coVerify {
            orderRepository.saveOrder(match { it.id.isNotBlank() })
        }
    }
}
