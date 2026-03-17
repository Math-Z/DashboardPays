package com.example.dashboardpays.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dashboardpays.data.local.entities.CountryEntity
import com.example.dashboardpays.data.local.entities.TranslationEntity
import com.example.dashboardpays.data.local.dao.CountryDao

@Database(
    entities = [CountryEntity::class, TranslationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
}