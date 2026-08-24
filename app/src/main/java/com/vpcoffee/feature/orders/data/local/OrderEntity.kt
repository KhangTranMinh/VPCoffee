package com.vpcoffee.feature.orders.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val sentAt: Long? = null,
)

@Entity(tableName = "order_items", primaryKeys = ["orderId", "drinkId"])
data class OrderItemEntity(
    val orderId: String,
    val drinkId: String,
    val drinkName: String,
    val unitPrice: Long,
    val quantity: Int,
)

data class OrderWithItems(
    @androidx.room.Embedded val order: OrderEntity,
    @androidx.room.Relation(parentColumn = "id", entityColumn = "orderId") val items: List<OrderItemEntity>,
)

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OrderWithItems>>

    @Upsert
    suspend fun upsertOrder(order: OrderEntity): Long

    @Upsert
    suspend fun upsertItems(items: List<OrderItemEntity>)

    @Query("UPDATE orders SET sentAt = :sentAt WHERE id IN (:orderIds)")
    suspend fun markAsSent(orderIds: List<String>, sentAt: Long)
}
