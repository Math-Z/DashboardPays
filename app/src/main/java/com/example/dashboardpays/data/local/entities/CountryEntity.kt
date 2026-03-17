package com.example.dashboardpays.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey
    val id: String,          // e.g., "FRA" (cca3)
    val englishName: String, // English name
    val capital: String?,
    val currency: String?,
    val flagLink: String
)