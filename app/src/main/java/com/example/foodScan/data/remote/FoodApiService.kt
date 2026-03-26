package com.example.foodScan.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface FoodApiService {
    // On utilise l'API v2 d'Open Food Facts
    // L'URL de base sera : https://world.openfoodfacts.org/api/v2/
    @GET("product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): ProductResponse
}