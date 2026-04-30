package com.example.foodScan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.foodScan.ui.components.ProductDetailSheet
import com.example.foodScan.data.domain.model.Product
import com.example.foodScan.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ProductViewModel, modifier: Modifier = Modifier) {

    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val availableAllergens by viewModel.availableAllergens.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val excludedAllergens by viewModel.excludedAllergens.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()

    var showBarcodeDialog by remember { mutableStateOf(false) }
    var allergenDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showBarcodeDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Saisir un code-barres")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Produits scannés",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )

            // Filter row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                FilterChip(
                    selected = showFavoritesOnly,
                    onClick = { viewModel.toggleFavoritesFilter() },
                    label = { Text("Favoris") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (showFavoritesOnly)
                                Icons.Filled.Favorite
                            else
                                Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Box {
                    FilterChip(
                        selected = excludedAllergens.isNotEmpty(),
                        onClick = { allergenDropdownExpanded = true },
                        label = {
                            Text(
                                if (excludedAllergens.isEmpty()) "Allergènes"
                                else "Allergènes (${excludedAllergens.size})"
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = allergenDropdownExpanded,
                        onDismissRequest = { allergenDropdownExpanded = false }
                    ) {
                        if (availableAllergens.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Aucun allergène trouvé",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {}
                            )
                        } else {
                            if (excludedAllergens.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Tout effacer",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        viewModel.clearAllergenFilters()
                                        allergenDropdownExpanded = false
                                    }
                                )
                                HorizontalDivider()
                            }
                            availableAllergens.forEach { allergen ->
                                DropdownMenuItem(
                                    text = { Text(allergen) },
                                    onClick = { viewModel.toggleAllergenExclusion(allergen) },
                                    trailingIcon = {
                                        Checkbox(
                                            checked = allergen in excludedAllergens,
                                            onCheckedChange = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Box {
                    FilterChip(
                        selected = selectedCategories.isNotEmpty(),
                        onClick = { categoryDropdownExpanded = true },
                        label = {
                            Text(
                                if (selectedCategories.isEmpty()) "Catégories"
                                else "Catégories (${selectedCategories.size})"
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        if (availableCategories.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Aucune catégorie trouvée",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {}
                            )
                        } else {
                            if (selectedCategories.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Tout effacer",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        viewModel.clearCategoryFilters()
                                        categoryDropdownExpanded = false
                                    }
                                )
                                HorizontalDivider()
                            }
                            availableCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = { viewModel.toggleCategorySelection(category) },
                                    trailingIcon = {
                                        Checkbox(
                                            checked = category in selectedCategories,
                                            onCheckedChange = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun produit trouvé",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(products, key = { it.barcode }) { product ->
                        ProductCard(
                            product = product,
                            onToggleFavorite = { viewModel.toggleFavorite(product.barcode) },
                            onClick = {
                                selectedProduct = product
                                scope.launch { detailSheetState.show() }
                            }
                        )
                    }
                }
            }
        }

        // Barcode dialog
        if (showBarcodeDialog) {
            BarcodeInputDialog(
                onConfirm = { barcode ->
                    viewModel.loadProduct(barcode)
                    showBarcodeDialog = false
                },
                onDismiss = { showBarcodeDialog = false }
            )
        }

        // Product detail sheet
        selectedProduct?.let { product ->
            ModalBottomSheet(
                onDismissRequest = { selectedProduct = null },
                sheetState = detailSheetState
            ) {
                ProductDetailSheet(
                    product = product,
                    onToggleFavorite = {
                        viewModel.toggleFavorite(product.barcode)
                        // Reflect updated favorite state live
                        selectedProduct = products.find { it.barcode == product.barcode }
                    }
                )
            }
        }
    }
}

@Composable
private fun BarcodeInputDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val isValid = input.length in 8..14 && input.all { it.isDigit() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Saisir un code-barres") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.length <= 14) input = it },
                label = { Text("Code-barres (8 à 14 chiffres)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (isValid) onConfirm(input) }
                ),
                isError = input.isNotEmpty() && !isValid,
                supportingText = {
                    if (input.isNotEmpty() && !isValid) Text("Code invalide")
                },
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(input) }, enabled = isValid) {
                Text("Rechercher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun ProductCard(
    product: Product,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                product.category?.let {
                    Text(
                        text = it.split(",").first().trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (product.allergens.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(product.allergens) { allergen ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = allergen,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (product.isFavorite)
                        Icons.Filled.Favorite
                    else
                        Icons.Filled.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (product.isFavorite)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}