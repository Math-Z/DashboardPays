package com.example.dashboardpays.data.repository // Vérifie bien ton package ici

import com.example.dashboardpays.data.local.dao.ProductDao
import com.example.dashboardpays.data.local.entities.*
import com.example.dashboardpays.data.remote.FoodApiService
import com.example.dashboardpays.data.remote.toDatabaseEntities

class ProductRepository(
    private val productDao: ProductDao,
    private val apiService: FoodApiService
) {
    suspend fun getFullProduct(barcode: String) {
        // On récupère depuis l'API
        val response = apiService.getProduct(barcode)

        // On utilise notre Mapper
        val (product, nutriments, allergens) = response.toDatabaseEntities()

        // On insère tout dans Room
        productDao.insertProduct(product)
        nutriments?.let { productDao.insertNutriments(it) }

        allergens.forEach { allergen ->
            productDao.insertAllergen(allergen)
            productDao.insertProductAllergenCrossRef(
                ProductAllergenCrossRef(product.id, allergen.id)
            )
        }
    }
}