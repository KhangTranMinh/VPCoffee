package com.vpcoffee.core.di

import android.content.Context
import androidx.room.Room
import com.vpcoffee.core.data.local.AppDatabase
import com.vpcoffee.feature.catalog.data.repository.DrinkRepositoryImpl
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import com.vpcoffee.feature.orders.data.repository.OrderRepositoryImpl
import com.vpcoffee.feature.orders.domain.repository.OrderRepository

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "vpcoffee.db",
    ).build()

    val drinkRepository: DrinkRepository = DrinkRepositoryImpl(database.drinkDao())
    val orderRepository: OrderRepository = OrderRepositoryImpl(database, database.orderDao())
}
