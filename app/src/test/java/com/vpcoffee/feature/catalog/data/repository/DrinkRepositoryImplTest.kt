package com.vpcoffee.feature.catalog.data.repository

import com.vpcoffee.feature.catalog.data.local.DrinkDao
import com.vpcoffee.feature.catalog.data.local.DrinkEntity
import com.vpcoffee.feature.catalog.domain.model.Drink
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
class DrinkRepositoryImplTest {

    private lateinit var drinkDao: DrinkDao
    private lateinit var repository: DrinkRepositoryImpl

    @Before
    fun setup() {
        drinkDao = mockk()
        repository = DrinkRepositoryImpl(drinkDao)
    }

    @Test
    fun `observeDrinks maps entities to domain models`() = runTest {
        val entities = listOf(
            DrinkEntity("1", "Coffee", 25000, null),
            DrinkEntity("2", "Tea", 20000, "image.jpg"),
        )
        every { drinkDao.observeAll() } returns flowOf(entities)

        val result = repository.observeDrinks().first()

        assertEquals(2, result.size)
        assertEquals(Drink("1", "Coffee", 25000, null), result[0])
        assertEquals(Drink("2", "Tea", 20000, "image.jpg"), result[1])
    }

    @Test
    fun `observeDrinks returns empty list when dao returns empty`() = runTest {
        every { drinkDao.observeAll() } returns flowOf(emptyList())

        val result = repository.observeDrinks().first()

        assertEquals(0, result.size)
    }

    @Test
    fun `saveDrink calls dao upsert with trimmed name`() = runTest {
        coEvery { drinkDao.upsert(any()) } returns 1L
        val drink = Drink("1", "  Coffee  ", 25000, null)

        repository.saveDrink(drink)

        coVerify {
            drinkDao.upsert(match {
                it.id == "1" && it.name == "Coffee" && it.price == 25000L
            })
        }
    }

    @Test
    fun `saveDrink returns drink id`() = runTest {
        coEvery { drinkDao.upsert(any()) } returns 1L

        val result = repository.saveDrink(Drink("1", "Coffee", 25000))

        assertEquals("1", result)
    }

    @Test
    fun `deleteDrink calls dao delete`() = runTest {
        coEvery { drinkDao.delete(any()) } returns Unit

        repository.deleteDrink("1")

        coVerify { drinkDao.delete("1") }
    }
}
