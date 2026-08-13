package com.vpcoffee.domain.repository

import com.vpcoffee.domain.model.Drink
import kotlinx.coroutines.flow.Flow

interface DrinkRepository {
    fun observeDrinks(): Flow<List<Drink>>
    suspend fun saveDrink(drink: Drink): Long
    suspend fun deleteDrink(id: Long)
}
