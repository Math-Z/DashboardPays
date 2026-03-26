package com.example.foodScan.data.domain.repository

import com.example.foodScan.data.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProduct(barcode: String): Product?

    fun getAllProducts(): Flow<List<Product>>
}