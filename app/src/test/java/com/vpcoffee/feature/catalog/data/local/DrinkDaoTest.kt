package com.vpcoffee.feature.catalog.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vpcoffee.core.data.local.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DrinkDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var drinkDao: DrinkDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        drinkDao = database.drinkDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `observeAll returns empty list when no drinks`() = runTest {
        val result = drinkDao.observeAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `upsert inserts drink`() = runTest {
        val drink = DrinkEntity("1", "Coffee", 25000, null)
        drinkDao.upsert(drink)

        val result = drinkDao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("Coffee", result[0].name)
        assertEquals(25000, result[0].price)
    }

    @Test
    fun `upsert updates existing drink`() = runTest {
        drinkDao.upsert(DrinkEntity("1", "Coffee", 25000, null))
        drinkDao.upsert(DrinkEntity("1", "Espresso", 30000, null))

        val result = drinkDao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("Espresso", result[0].name)
        assertEquals(30000, result[0].price)
    }

    @Test
    fun `observeAll orders by name`() = runTest {
        drinkDao.upsert(DrinkEntity("1", "Tea", 20000, null))
        drinkDao.upsert(DrinkEntity("2", "Coffee", 25000, null))
        drinkDao.upsert(DrinkEntity("3", "Americano", 22000, null))

        val result = drinkDao.observeAll().first()
        assertEquals(3, result.size)
        assertEquals("Americano", result[0].name)
        assertEquals("Coffee", result[1].name)
        assertEquals("Tea", result[2].name)
    }

    @Test
    fun `delete removes drink`() = runTest {
        drinkDao.upsert(DrinkEntity("1", "Coffee", 25000, null))
        drinkDao.upsert(DrinkEntity("2", "Tea", 20000, null))
        drinkDao.delete("1")

        val result = drinkDao.observeAll().first()
        assertEquals(1, result.size)
        assertEquals("Tea", result[0].name)
    }

    @Test
    fun `delete does nothing when id does not exist`() = runTest {
        drinkDao.upsert(DrinkEntity("1", "Coffee", 25000, null))
        drinkDao.delete("999")

        val result = drinkDao.observeAll().first()
        assertEquals(1, result.size)
    }

    @Test
    fun `upsert preserves imageUri`() = runTest {
        drinkDao.upsert(DrinkEntity("1", "Coffee", 25000, "image.jpg"))

        val result = drinkDao.observeAll().first()
        assertEquals("image.jpg", result[0].imageUri)
    }

    @Test
    fun `upsert handles null imageUri`() = runTest {
        drinkDao.upsert(DrinkEntity("1", "Coffee", 25000, null))

        val result = drinkDao.observeAll().first()
        assertEquals(null, result[0].imageUri)
    }

    @Test
    fun `upsert handles multiple drinks`() = runTest {
        drinkDao.upsert(DrinkEntity("1", "Coffee", 25000, null))
        drinkDao.upsert(DrinkEntity("2", "Tea", 20000, null))
        drinkDao.upsert(DrinkEntity("3", "Americano", 22000, null))

        val result = drinkDao.observeAll().first()
        assertEquals(3, result.size)
    }

    @Test
    fun `delete removes correct drink`() = runTest {
        drinkDao.upsert(DrinkEntity("1", "Coffee", 25000, null))
        drinkDao.upsert(DrinkEntity("2", "Tea", 20000, null))
        drinkDao.upsert(DrinkEntity("3", "Americano", 22000, null))
        drinkDao.delete("2")

        val result = drinkDao.observeAll().first()
        assertEquals(2, result.size)
        assertTrue(result.none { it.id == "2" })
    }
}
