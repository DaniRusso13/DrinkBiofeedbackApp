package com.example.drinkbiofeedback20.main.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import android.content.Context

@Database(entities = [DrinkVolume::class], version = 2, exportSchema = false)
abstract class DrinkDataBase : RoomDatabase() {
    abstract fun drinkDataBaseDao(): DrinkDataBaseDao
    companion object {
        //private nullable variable and initialized to null
        @Volatile
        private var INSTANCE: DrinkDataBase? = null

        fun getInstance(context: Context): DrinkDataBase {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this) {
                var instance = INSTANCE
                //check if already exist a database
                if(instance==null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        DrinkDataBase::class.java,
                        "drink_history_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

}