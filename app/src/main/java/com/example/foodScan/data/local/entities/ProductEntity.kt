package com.example.foodScan.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String, // Code-barres
    val name: String,
    val imageUrl: String?,
    val category: String?
)