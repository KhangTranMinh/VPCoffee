package com.vpcoffee.data.repository

import androidx.room.withTransaction
import com.vpcoffee.data.local.AppDatabase
import com.vpcoffee.data.local.OrderDao
import com.vpcoffee.data.local.OrderEntity
import com.vpcoffee.data.local.OrderItemEntity
import com.vpcoffee.domain.model.Order
import com.vpcoffee.domain.model.OrderItem
import com.vpcoffee.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl(
    private val database: AppDatabase,
    private val orderDao: OrderDao,
) : OrderRepository {
    override fun observeOrders(): Flow<List<Order>> = orderDao.observeAll().map { orders ->
        orders.map { order ->
            Order(
                id = order.order.id,
                createdAt = order.order.createdAt,
                items = order.items.map { item ->
                    OrderItem(item.drinkId, item.drinkName, item.unitPrice, item.quantity)
                },
            )
        }
    }

    override suspend fun saveOrder(order: Order): Long = database.withTransaction {
        val orderId = orderDao.upsertOrder(OrderEntity(order.id, order.createdAt))
        orderDao.upsertItems(order.items.map { item ->
            OrderItemEntity(orderId, item.drinkId, item.drinkName, item.unitPrice, item.quantity)
        })
        orderId
    }
}
