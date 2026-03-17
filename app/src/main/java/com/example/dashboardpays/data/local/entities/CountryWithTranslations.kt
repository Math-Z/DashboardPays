package com.example.dashboardpays.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class CountryWithTranslations(
    @Embedded val country: CountryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "countryId"
    )
    val translations: List<TranslationEntity>
)