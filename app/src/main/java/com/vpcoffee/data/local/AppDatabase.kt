package com.vpcoffee.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DrinkEntity::class, OrderEntity::class, OrderItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDao
    abstract fun orderDao(): OrderDao
}
