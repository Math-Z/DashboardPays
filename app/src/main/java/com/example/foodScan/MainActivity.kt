package com.example.foodScan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.foodScan.data.local.AppDatabase
import com.example.foodScan.data.repository.ProductRepositoryImpl
import com.example.foodScan.ui.navbar.MyBottomBar
import com.example.foodScan.ui.theme.FoodScanTheme
import com.example.foodScan.viewmodel.ProductViewModel
import com.example.foodScan.data.remote.FoodApiService
import com.example.foodScan.viewmodel.ProductViewModelFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Configuration de l'API (Retrofit + Serialization)
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true // Évite les crashs si l'API ajoute des champs
            coerceInputValues = true
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/api/v2/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        val apiService = retrofit.create(FoodApiService::class.java)

        // 2. Initialisation de la DB (via ton Singleton)
        val database = AppDatabase.getDatabase(this)

        // 3. Création du Repository (Implémentation)
        val repository = ProductRepositoryImpl(database.productDao(), apiService)

        // 4. Initialisation du ViewModel via la Factory
        // C'est ce délégué 'by viewModels' qui permet au VM de ne pas être recréé au scroll/rotation
        val viewModel: ProductViewModel by viewModels {
            ProductViewModelFactory(repository)
        }

        setContent {
            FoodScanTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { MyBottomBar() }
                ) { innerPadding ->
                    // Ici tu appelles ton NavGraph et tu lui passes le viewModel
                    // AppNavGraph(viewModel = viewModel, modifier = Modifier.padding(innerPadding))

                    Text(
                        text = "Prêt à scanner !",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FoodScanTheme {
        Greeting("Android")
    }
}