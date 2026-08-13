package com.vpcoffee.domain.repository

import com.vpcoffee.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<Order>>
    suspend fun saveOrder(order: Order): Long
}
