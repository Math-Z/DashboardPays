package com.example.foodScan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.foodScan.data.domain.repository.ProductRepository

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {


}