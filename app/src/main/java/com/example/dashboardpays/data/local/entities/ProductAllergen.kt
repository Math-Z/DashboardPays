package com.example.dashboardpays.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "product_allergen_cross_ref",
    primaryKeys = ["productId", "allergenId"]
)
data class ProductAllergenCrossRef(
    val productId: String,
    val allergenId: String
)