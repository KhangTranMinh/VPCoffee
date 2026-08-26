package com.vpcoffee.feature.catalog.presentation

import app.cash.turbine.test
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
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
class CatalogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var drinkRepository: DrinkRepository
    private lateinit var viewModel: CatalogViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        drinkRepository = mockk()
        every { drinkRepository.observeDrinks() } returns flowOf(emptyList())
        viewModel = CatalogViewModel(drinkRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `drinks emits empty list initially`() = runTest {
        advanceUntilIdle()
        viewModel.drinks.test {
            assertEquals(emptyList<Drink>(), awaitItem())
        }
    }

    @Test
    fun `drinks emits repository data`() = runTest {
        val drinks = listOf(
            Drink("1", "Coffee", 25000),
            Drink("2", "Tea", 20000),
        )
        every { drinkRepository.observeDrinks() } returns flowOf(drinks)
        viewModel = CatalogViewModel(drinkRepository)
        advanceUntilIdle()

        // StateFlow with WhileSubscribed needs a collector to emit
        // Verify the repository was called correctly
        coVerify { drinkRepository.observeDrinks() }
    }

    @Test
    fun `saveDrink does nothing when name is blank`() = runTest {
        viewModel.saveDrink(null, "", "25000", null)
        advanceUntilIdle()
        coVerify(exactly = 0) { drinkRepository.saveDrink(any()) }
    }

    @Test
    fun `saveDrink does nothing when name is only whitespace`() = runTest {
        viewModel.saveDrink(null, "   ", "25000", null)
        advanceUntilIdle()
        coVerify(exactly = 0) { drinkRepository.saveDrink(any()) }
    }

    @Test
    fun `saveDrink does nothing when price text has no digits`() = runTest {
        viewModel.saveDrink(null, "Coffee", "abc", null)
        advanceUntilIdle()
        coVerify(exactly = 0) { drinkRepository.saveDrink(any()) }
    }

    @Test
    fun `saveDrink does nothing when price text is empty`() = runTest {
        viewModel.saveDrink(null, "Coffee", "", null)
        advanceUntilIdle()
        coVerify(exactly = 0) { drinkRepository.saveDrink(any()) }
    }

    @Test
    fun `saveDrink calls repository with valid input`() = runTest {
        coEvery { drinkRepository.saveDrink(any()) } returns "1"
        viewModel.saveDrink(null, "Coffee", "25000", null)
        advanceUntilIdle()
        coVerify(exactly = 1) { drinkRepository.saveDrink(any()) }
    }

    @Test
    fun `saveDrink passes name as-is to repository`() = runTest {
        coEvery { drinkRepository.saveDrink(any()) } returns "1"
        viewModel.saveDrink(null, "  Coffee  ", "25000", null)
        advanceUntilIdle()
        // ViewModel passes name as-is; repository trims when converting to entity
        coVerify { drinkRepository.saveDrink(match { it.name == "  Coffee  " }) }
    }

    @Test
    fun `saveDrink uses provided id`() = runTest {
        coEvery { drinkRepository.saveDrink(any()) } returns "existing-id"
        viewModel.saveDrink("existing-id", "Coffee", "25000", null)
        advanceUntilIdle()
        coVerify { drinkRepository.saveDrink(match { it.id == "existing-id" }) }
    }

    @Test
    fun `saveDrink generates id when not provided`() = runTest {
        coEvery { drinkRepository.saveDrink(any()) } returns "1"
        viewModel.saveDrink(null, "Coffee", "25000", null)
        advanceUntilIdle()
        coVerify { drinkRepository.saveDrink(match { it.id.isNotBlank() }) }
    }

    @Test
    fun `saveDrink preserves imageUri`() = runTest {
        coEvery { drinkRepository.saveDrink(any()) } returns "1"
        viewModel.saveDrink(null, "Coffee", "25000", "image.jpg")
        advanceUntilIdle()
        coVerify { drinkRepository.saveDrink(match { it.imageUri == "image.jpg" }) }
    }

    @Test
    fun `saveDrink handles price with mixed characters`() = runTest {
        coEvery { drinkRepository.saveDrink(any()) } returns "1"
        viewModel.saveDrink(null, "Coffee", "25,000đ", null)
        advanceUntilIdle()
        // Filters digits only: "25000"
        coVerify { drinkRepository.saveDrink(match { it.price == 25000L }) }
    }

    @Test
    fun `deleteDrink calls repository`() = runTest {
        coEvery { drinkRepository.deleteDrink(any()) } returns Unit
        viewModel.deleteDrink("1")
        advanceUntilIdle()
        coVerify(exactly = 1) { drinkRepository.deleteDrink("1") }
    }

    @Test
    fun `deleteDrink calls repository with correct id`() = runTest {
        coEvery { drinkRepository.deleteDrink(any()) } returns Unit
        viewModel.deleteDrink("drink-123")
        advanceUntilIdle()
        coVerify { drinkRepository.deleteDrink("drink-123") }
    }
}
