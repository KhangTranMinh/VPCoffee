package com.vpcoffee

import android.content.Context
import androidx.room.Room
import com.vpcoffee.data.local.AppDatabase
import com.vpcoffee.data.repository.DrinkRepositoryImpl
import com.vpcoffee.domain.repository.DrinkRepository

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "vpcoffee.db",
    ).build()

    val drinkRepository: DrinkRepository = DrinkRepositoryImpl(database.drinkDao())
}
