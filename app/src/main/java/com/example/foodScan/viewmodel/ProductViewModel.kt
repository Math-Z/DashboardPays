package com.example.foodScan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodScan.data.domain.model.Product
import com.example.foodScan.data.domain.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    // Now a Set — products containing ANY selected allergen are excluded
    private val _excludedAllergens = MutableStateFlow<Set<String>>(emptySet())
    val excludedAllergens: StateFlow<Set<String>> = _excludedAllergens.asStateFlow()

    private val _allProducts: StateFlow<List<Product>> = repository
        .getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val filteredProducts: StateFlow<List<Product>> = combine(
        _allProducts,
        _showFavoritesOnly,
        _excludedAllergens
    ) { products, favoritesOnly, excluded ->
        products
            .filter { if (favoritesOnly) it.isFavorite else true }
            .filter { product ->
                if (excluded.isEmpty()) true
                else product.allergens.none { it in excluded }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val availableAllergens: StateFlow<List<String>> = _allProducts
        .map { products -> products.flatMap { it.allergens }.distinct().sorted() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _currentProduct = MutableStateFlow<Product?>(null)
    val currentProduct: StateFlow<Product?> = _currentProduct.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun toggleAllergenExclusion(allergen: String) {
        _excludedAllergens.update { current ->
            if (allergen in current) current - allergen else current + allergen
        }
    }

    fun clearAllergenFilters() {
        _excludedAllergens.value = emptySet()
    }

    fun toggleFavorite(barcode: String) {
        viewModelScope.launch {
            repository.toggleFavorite(barcode)
        }
    }

    fun loadProduct(barcode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _currentProduct.value = repository.getProduct(barcode)
            } catch (e: Exception) {
                _error.value = "Produit introuvable : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCurrentProduct() {
        _currentProduct.value = null
        _error.value = null
    }
}