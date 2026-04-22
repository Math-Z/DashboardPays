package com.example.foodScan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.foodScan.data.local.dao.ProductDao
import com.example.foodScan.data.local.entities.AllergenEntity
import com.example.foodScan.data.local.entities.NutrimentEntity
import com.example.foodScan.data.local.entities.ProductAllergenCrossRef
import com.example.foodScan.data.local.entities.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        NutrimentEntity::class,
        AllergenEntity::class,
        ProductAllergenCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_scan_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}