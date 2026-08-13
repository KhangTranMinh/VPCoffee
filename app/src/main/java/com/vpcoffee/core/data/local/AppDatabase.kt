package com.vpcoffee.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vpcoffee.feature.catalog.data.local.DrinkDao
import com.vpcoffee.feature.catalog.data.local.DrinkEntity
import com.vpcoffee.feature.orders.data.local.OrderDao
import com.vpcoffee.feature.orders.data.local.OrderEntity
import com.vpcoffee.feature.orders.data.local.OrderItemEntity

@Database(
    entities = [DrinkEntity::class, OrderEntity::class, OrderItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
    abstract fun orderDao(): OrderDao
}
