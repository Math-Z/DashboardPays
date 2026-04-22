package com.example.foodScan.data.repository

import com.example.foodScan.data.domain.model.Nutriments
import com.example.foodScan.data.domain.model.Product
import com.example.foodScan.data.local.dao.FullProduct
import com.example.foodScan.data.local.dao.ProductDao
import com.example.foodScan.data.local.entities.ProductAllergenCrossRef
import com.example.foodScan.data.remote.FoodApiService
import com.example.foodScan.data.remote.toDatabaseEntities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val apiService: FoodApiService
) : com.example.foodScan.data.domain.repository.ProductRepository {

    override suspend fun getProduct(barcode: String): Product? {
        val localProduct = productDao.getProductWithDetails(barcode)
        return if (localProduct != null) {
            localProduct.toDomain()
        } else {
            refreshProductFromApi(barcode)
            productDao.getProductWithDetails(barcode)?.toDomain()
        }
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProductsWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun toggleFavorite(barcode: String) {
        productDao.toggleFavorite(barcode)
    }

    private suspend fun refreshProductFromApi(barcode: String) {
        try {
            val response = apiService.getProduct(barcode)
            val (product, nutriments, allergens) = response.toDatabaseEntities()

            productDao.insertProduct(product)
            nutriments?.let { productDao.insertNutriments(it) }
            allergens.forEach { allergen ->
                productDao.insertAllergen(allergen)
                productDao.insertProductAllergenCrossRef(
                    ProductAllergenCrossRef(
                        productId = product.id,
                        allergenId = allergen.id
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun FullProduct.toDomain(): Product {
        return Product(
            barcode = this.product.id,
            name = this.product.name,
            imageUrl = this.product.imageUrl,
            category = this.product.category,
            isFavorite = this.product.isFavorite,
            nutriments = this.nutriments?.let {
                Nutriments(
                    energy = it.energy ?: 0.0,
                    energyKj = it.energyKj ?: 0.0,
                    fat = it.fat ?: 0.0,
                    saturatedFat = it.saturatedFat ?: 0.0,
                    carbohydrates = it.carbohydrates ?: 0.0,
                    sugar = it.sugar ?: 0.0,
                    protein = it.protein ?: 0.0,
                    salt = it.salt ?: 0.0,
                    fiber = it.fiber ?: 0.0,
                    sodium = it.sodium ?: 0.0
                )
            },
            allergens = this.allergens.map { it.label }
        )
    }
}