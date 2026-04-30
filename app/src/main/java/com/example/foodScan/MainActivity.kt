package com.example.foodScan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodScan.data.local.AppDatabase
import com.example.foodScan.data.remote.FoodApiService
import com.example.foodScan.data.repository.ProductRepositoryImpl
import com.example.foodScan.ui.navbar.MyBottomBar
import com.example.foodScan.ui.screens.HomeScreen
import com.example.foodScan.ui.screens.ScannerScreen
import com.example.foodScan.ui.theme.FoodScanTheme
import com.example.foodScan.viewmodel.ProductViewModel
import com.example.foodScan.viewmodel.ProductViewModelFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/api/v2/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val repository = ProductRepositoryImpl(
            AppDatabase.getDatabase(this).productDao(),
            retrofit.create(FoodApiService::class.java)
        )
        val viewModel: ProductViewModel by viewModels {
            ProductViewModelFactory(repository)
        }

        setContent {
            FoodScanTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MyBottomBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(viewModel) }
                        composable("scanner") { ScannerScreen(viewModel) }
                    }
                }
            }
        }
    }
}