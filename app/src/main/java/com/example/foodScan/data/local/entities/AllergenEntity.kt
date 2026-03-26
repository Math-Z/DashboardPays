package com.example.foodScan.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allergens")
data class AllergenEntity(
    @PrimaryKey val id: String, // ex: "en:milk"
    val label: String           // ex: "Lait"
)