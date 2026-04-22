package com.example.foodScan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foodScan.data.domain.model.Product
import com.example.foodScan.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ProductViewModel, modifier: Modifier = Modifier) {

    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val availableAllergens by viewModel.availableAllergens.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val excludedAllergens by viewModel.excludedAllergens.collectAsStateWithLifecycle()

    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showAllergenSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Favorites chip
                item {
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
                }

                // Allergen filter chip — shows count of active exclusions
                if (availableAllergens.isNotEmpty()) {
                    item {
                        FilterChip(
                            selected = excludedAllergens.isNotEmpty(),
                            onClick = { showAllergenSheet = true },
                            label = {
                                Text(
                                    if (excludedAllergens.isEmpty()) "Allergènes"
                                    else "Allergènes (${excludedAllergens.size})"
                                )
                            }
                        )
                    }
                }
            }

            // Product list
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
                            onToggleFavorite = { viewModel.toggleFavorite(product.barcode) }
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

        // Allergen bottom sheet
        if (showAllergenSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAllergenSheet = false },
                sheetState = sheetState
            ) {
                AllergenFilterSheet(
                    allergens = availableAllergens,
                    excludedAllergens = excludedAllergens,
                    onToggle = { viewModel.toggleAllergenExclusion(it) },
                    onClear = { viewModel.clearAllergenFilters() },
                    onDone = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion { showAllergenSheet = false }
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
                    if (input.isNotEmpty() && !isValid)
                        Text("Code invalide")
                },
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(input) },
                enabled = isValid
            ) {
                Text("Rechercher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun AllergenFilterSheet(
    allergens: List<String>,
    excludedAllergens: Set<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exclure les allergènes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (excludedAllergens.isNotEmpty()) {
                TextButton(onClick = onClear) { Text("Tout effacer") }
            }
        }

        Text(
            text = "Les produits contenant les allergènes sélectionnés seront masqués.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider()

        // Allergen checkboxes
        allergens.forEach { allergen ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = allergen in excludedAllergens,
                    onCheckedChange = { onToggle(allergen) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = allergen,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

        // Done button
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Appliquer")
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                product.category?.let {
                    Text(
                        text = it,
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