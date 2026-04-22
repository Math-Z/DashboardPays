package com.example.foodScan.data.remote

import com.example.foodScan.data.local.entities.*

fun ProductResponse.toDatabaseEntities(): Triple<ProductEntity, NutrimentEntity?, List<AllergenEntity>> {
    val apiProduct = this.product ?: throw Exception("Produit non trouvé")

    val product = ProductEntity(
        id = apiProduct.id,
        name = apiProduct.name ?: "Inconnu",
        imageUrl = apiProduct.imageUrl,
        category = apiProduct.categories
    )

    val nutriments = apiProduct.nutriments?.let {
        NutrimentEntity(
            productId = apiProduct.id,
            energy = it.energy,
            energyKj = it.energyKj,
            fat = it.fat,
            saturatedFat = it.saturatedFat,
            carbohydrates = it.carbohydrates,
            sugar = it.sugar,
            protein = it.protein,
            salt = it.salt,
            fiber = it.fiber,
            sodium = it.sodium
        )
    }

    val allergens = apiProduct.allergensTags.map { tag ->
        AllergenEntity(
            id = tag,
            label = tag.substringAfter(":").replaceFirstChar { it.uppercase() }
        )
    }

    return Triple(product, nutriments, allergens)
}