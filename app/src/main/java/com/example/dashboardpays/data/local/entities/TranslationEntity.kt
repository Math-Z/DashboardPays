package com.example.dashboardpays.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "translations",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["countryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("countryId")]
)
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val countryId: String,        // FK to CountryEntity
    val languageCode: String,     // ISO 639-3, e.g., "fra", "deu"
    val commonName: String,
    val officialName: String? = null
)