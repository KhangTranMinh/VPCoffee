package com.vpcoffee.feature.catalog.data.repository

import com.vpcoffee.feature.catalog.data.local.DrinkDao
import com.vpcoffee.feature.catalog.data.local.DrinkEntity
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DrinkRepositoryImpl(private val drinkDao: DrinkDao) : DrinkRepository {
    override fun observeDrinks(): Flow<List<Drink>> = drinkDao.observeAll().map { drinks ->
        drinks.map { it.toDomain() }
    }

    override suspend fun saveDrink(drink: Drink): String {
        drinkDao.upsert(drink.toEntity())
        return drink.id
    }

    override suspend fun deleteDrink(id: String) = drinkDao.delete(id)
}

private fun DrinkEntity.toDomain() = Drink(id, name, price, imageUri)

private fun Drink.toEntity() = DrinkEntity(id, name.trim(), price, imageUri)
