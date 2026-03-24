package com.example.dashboardpays.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dashboardpays.data.local.dao.ProductDao
import com.example.dashboardpays.data.local.entities.ProductEntity
import com.example.dashboardpays.data.local.entities.NutrimentEntity
import com.example.dashboardpays.data.local.entities.AllergenEntity
import com.example.dashboardpays.data.local.entities.ProductAllergenCrossRef

@Database(
    entities = [
        ProductEntity::class,
        NutrimentEntity::class,
        AllergenEntity::class,
        ProductAllergenCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Renomme la fonction pour qu'elle soit cohérente avec l'objet retourné
    abstract fun productDao(): ProductDao
}