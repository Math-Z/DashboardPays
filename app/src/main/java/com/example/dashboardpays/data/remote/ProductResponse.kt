package com.example.dashboardpays.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val product: ApiProduct? = null,
    val status: Int? = null
)

@Serializable
data class ApiProduct(
    @SerialName("_id") val id: String,
    @SerialName("product_name") val name: String? = "Inconnu",
    @SerialName("image_url") val imageUrl: String? = null,
    val categories: String? = null,
    val nutriments: ApiNutriments? = null,
    @SerialName("allergens_tags") val allergensTags: List<String> = emptyList()
)

@Serializable
data class ApiNutriments(
    @SerialName("sugars_100g") val sugar: Double? = 0.0,
    @SerialName("salt_100g") val salt: Double? = 0.0,
    @SerialName("fat_100g") val fat: Double? = 0.0,
    @SerialName("energy-kcal_100g") val energy: Double? = 0.0,
    @SerialName("sodium_100g") val sodium: Double? = 0.0,
    @SerialName("proteins_100g") val protein: Double? = 0.0,
    @SerialName("fiber_100g") val fiber: Double? = 0.0
)