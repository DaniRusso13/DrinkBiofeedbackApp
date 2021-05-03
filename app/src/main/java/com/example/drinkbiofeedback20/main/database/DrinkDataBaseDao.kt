package com.example.drinkbiofeedback20.main.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DrinkDataBaseDao {
    @Insert
    suspend fun insert(volume: DrinkVolume)

    @Update
    fun update(volume: DrinkVolume)

    @Query("SELECT * from daily_liquid_volume_table WHERE volumeId = :key")
    fun get(key:Long): DrinkVolume

    @Query("DELETE FROM daily_liquid_volume_table")
    fun clear()

    @Query("SELECT * FROM daily_liquid_volume_table ORDER BY volumeId ASC")
    fun getAllData(): LiveData<List<DrinkVolume>>

    @Query("SELECT * FROM daily_liquid_volume_table ORDER BY volumeId DESC LIMIT 1")
    fun getTodayData(): DrinkVolume? //? stay for NULLABLE
}