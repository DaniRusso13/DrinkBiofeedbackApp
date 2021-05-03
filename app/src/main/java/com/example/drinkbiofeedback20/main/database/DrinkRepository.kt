package com.example.drinkbiofeedback20.main.database

import androidx.lifecycle.LiveData

class DrinkRepository(private val drinkDataBaseDao: DrinkDataBaseDao) {
    val readAllData: LiveData<List<DrinkVolume>> = drinkDataBaseDao.getAllData()

    suspend fun addVolume(drinkVolume: DrinkVolume) {
        drinkDataBaseDao.insert(drinkVolume)
    }
    suspend fun clearDatabase(){
        drinkDataBaseDao.clear()
    }
}