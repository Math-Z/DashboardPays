package com.example.foodScan.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy


import androidx.room.*
import com.example.foodScan.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // --- INSERTIONS ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutriments(nutriments: NutrimentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergen(allergen: AllergenEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductAllergenCrossRef(crossRef: ProductAllergenCrossRef)

    // --- LECTURE ---

    // Récupère un produit avec tous ses détails (Nutriments + Allergènes via la Junction)
    @Transaction
    @Query("SELECT * FROM products WHERE id = :barcode")
    suspend fun getProductWithDetails(barcode: String): FullProduct?

    // Pour afficher la liste de l'historique dans l'UI avec Flow (temps réel)
    @Transaction
    @Query("SELECT * FROM products")
    fun getAllProductsWithDetails(): Flow<List<FullProduct>>

    // --- SUPPRESSION ---

    @Query("DELETE FROM products WHERE id = :barcode")
    suspend fun deleteProduct(barcode: String)
}

/**
 * Cette classe n'est pas une @Entity, c'est un POJO pour Room
 * qui fait la jointure entre toutes tes tables.
 */
data class FullProduct(
    @Embedded val product: ProductEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val nutriments: NutrimentEntity?,

    @Relation(
        parentColumn = "id", // ID du produit
        entityColumn = "id", // ID de l'allergène
        associateBy = Junction(
            value = ProductAllergenCrossRef::class,
            parentColumn = "productId",
            entityColumn = "allergenId"
        )
    )
    val allergens: List<AllergenEntity>
)