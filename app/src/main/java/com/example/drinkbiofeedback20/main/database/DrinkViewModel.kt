package com.example.drinkbiofeedback20.main.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DrinkViewModel(val database: DrinkDataBaseDao, application: Application) : AndroidViewModel(application) {
    val getAllData: LiveData<List<DrinkVolume>>
    private val repository: DrinkRepository

    init {
        val drinkDataBaseDao = DrinkDataBase.getInstance(application).drinkDataBaseDao()
        repository = DrinkRepository(drinkDataBaseDao)
        getAllData = repository.readAllData
    }

    fun addVolume(volume: DrinkVolume){
        viewModelScope.launch(Dispatchers.IO){
            repository.addVolume(volume)
        }
    }
    fun deleteAllData(){
        viewModelScope.launch(Dispatchers.IO){
            repository.clearDatabase()
        }
    }
}