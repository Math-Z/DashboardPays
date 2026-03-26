package com.example.foodScan.data.domain.model

data class Product(
    val barcode: String,
    val name: String,
    val imageUrl: String?,
    val category: String?,
    val nutriments: Nutriments?,
    val allergens: List<String>
)

data class Nutriments(
    val energy: Double,
    val fat: Double,
    val sugar: Double,
    val protein: Double,
    val salt: Double,
    val fiber: Double,
    val sodium: Double
)