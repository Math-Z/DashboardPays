package com.example.foodScan.data.domain.repository

import com.example.foodScan.data.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    // On retourne le modèle propre (Domain), pas l'entité Room
    suspend fun getProduct(barcode: String): Product?

    fun getAllProducts(): Flow<List<Product>>
}