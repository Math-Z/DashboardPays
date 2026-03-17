package com.example.dashboardpays.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.dashboardpays.data.local.entities.CountryEntity
import com.example.dashboardpays.data.local.entities.CountryWithTranslations
import com.example.dashboardpays.data.local.entities.TranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {

    // Get all countries in English
    @Query("SELECT * FROM countries ORDER BY englishName ASC")
    fun getAllCountriesInEnglish(): Flow<List<CountryEntity>>

    // Get a country name in another language
    @Query("""
        SELECT *
        FROM translations
        WHERE countryId = :countryId AND languageCode = :langCode
        LIMIT 1
    """)
    suspend fun getTranslation(countryId: String, langCode: String): TranslationEntity?

    // Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationEntity>)
}