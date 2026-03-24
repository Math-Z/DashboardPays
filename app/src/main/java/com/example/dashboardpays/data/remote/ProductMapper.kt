package com.example.dashboardpays.data.remote

import com.example.dashboardpays.data.local.entities.*

// Cette fonction prend la réponse API et la transforme en tout ce dont Room a besoin
fun ProductResponse.toDatabaseEntities(): Triple<ProductEntity, NutrimentEntity?, List<AllergenEntity>> {
    val apiProduct = this.product ?: throw Exception("Produit non trouvé")

    // 1. Créer le produit
    val product = ProductEntity(
        id = apiProduct.id,
        name = apiProduct.name ?: "Inconnu",
        imageUrl = apiProduct.imageUrl,
        category = apiProduct.categories
    )

    // 2. Créer les nutriments
    val nutriments = apiProduct.nutriments?.let {
        NutrimentEntity(
            productId = apiProduct.id,
            sugar = it.sugar,
            salt = it.salt,
            fat = it.fat,
            energy = it.energy,
            sodium = it.sodium,
            protein = it.protein,
            fiber = it.fiber
        )
    }

    // 3. Créer les allergènes
    val allergens = apiProduct.allergensTags.map { tag ->
        AllergenEntity(
            id = tag,
            label = tag.substringAfter(":").replaceFirstChar { it.uppercase() } // "en:milk" -> "Milk"
        )
    }

    return Triple(product, nutriments, allergens)
}