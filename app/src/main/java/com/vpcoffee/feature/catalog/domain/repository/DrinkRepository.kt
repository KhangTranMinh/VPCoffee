package com.vpcoffee.feature.catalog.domain.repository

import com.vpcoffee.feature.catalog.domain.model.Drink
import kotlinx.coroutines.flow.Flow

interface DrinkRepository {
    fun observeDrinks(): Flow<List<Drink>>
    suspend fun saveDrink(drink: Drink): String
    suspend fun deleteDrink(id: String)
}
