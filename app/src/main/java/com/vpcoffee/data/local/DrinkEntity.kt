package com.vpcoffee.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Long,
    val imageUri: String?,
)

@Dao
interface DrinkDao {
    @Query("SELECT * FROM drinks ORDER BY name")
    fun observeAll(): Flow<List<DrinkEntity>>

    @Upsert
    suspend fun upsert(drink: DrinkEntity): Long

    @Query("DELETE FROM drinks WHERE id = :id")
    suspend fun delete(id: Long)
}
