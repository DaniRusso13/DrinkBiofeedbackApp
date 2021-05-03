package com.example.drinkbiofeedback20.main.database

import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate


@Entity(tableName = "daily_liquid_volume_table")
data class DrinkVolume(
    @PrimaryKey(autoGenerate = true)
    var volumeId: Int,
    @ColumnInfo(name = "date")
    val dateString: String,
    @ColumnInfo(name = "Time")
    var timeString: String,
    @ColumnInfo(name = "volume_of_liquid")
    var liquidVolume: Int
)
