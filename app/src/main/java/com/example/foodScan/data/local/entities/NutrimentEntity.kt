package com.example.foodScan.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "nutriments",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NutrimentEntity(
    @PrimaryKey val productId: String,
    val sugar: Double?,
    val salt: Double?,
    val fat: Double?,
    val energy: Double?,
    val sodium: Double?,
    val protein: Double?,
    val fiber: Double?
)