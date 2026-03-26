package com.example.foodScan.data.repository // Vérifie bien ton package ici

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

    // 2. On doit "Override" les fonctions de l'interface
    override suspend fun getProduct(barcode: String): Product? {
        // Regarder d'abord en local
        val localProduct = productDao.getProductWithDetails(barcode)

        return if (localProduct != null) {
            localProduct.toDomain()
        } else {
            // Si pas là, on fait ton travail de récupération API
            refreshProductFromApi(barcode)
            // On récupère ce qu'on vient d'insérer
            productDao.getProductWithDetails(barcode)?.toDomain()
        }
    }

    override fun getAllProducts(): Flow<List<Product>> {
        // On récupère le Flow de Room et on le transforme en Flow de Domain
        return productDao.getAllProductsWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    // Ton ancienne fonction "getFullProduct" devient une aide interne
    private suspend fun refreshProductFromApi(barcode: String) {
        try {
            val response = apiService.getProduct(barcode)
            val (product, nutriments, allergens) = response.toDatabaseEntities()

            // 1. Insertion du produit de base
            productDao.insertProduct(product)

            // 2. Insertion des nutriments (si présents)
            nutriments?.let { productDao.insertNutriments(it) }

            // 3. Insertion des allergènes et de la table de liaison
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
            // Log l'erreur ou gère le cas où le produit n'existe pas sur l'API
            e.printStackTrace()
        }
    }

    fun FullProduct.toDomain(): Product {
        return Product(
            barcode = this.product.id,
            name = this.product.name,
            imageUrl = this.product.imageUrl,
            category = this.product.category,
            nutriments = this.nutriments?.let {
                Nutriments(
                    energy = it.energy ?: 0.0,
                    fat = it.fat ?: 0.0,
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