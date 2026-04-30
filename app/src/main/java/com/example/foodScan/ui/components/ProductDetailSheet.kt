package com.example.foodScan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foodScan.data.domain.model.Product
import kotlin.text.split

@Composable
fun ProductDetailSheet(
    product: Product,
    onToggleFavorite: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header — image + name + favorite
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    product.category?.let {
                        Text(
                            text = it.split(",").first().trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

        // Barcode + full category
        item {
            SectionCard {
                DetailRow(label = "Code-barres", value = product.barcode)
                product.category?.let {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    DetailRow(label = "Catégorie", value = it)
                }
            }
        }

        // Allergens
        if (product.allergens.isNotEmpty()) {
            item {
                SectionTitle("Allergènes")
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(product.allergens) { allergen ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = allergen,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // Nutrients table
        product.nutriments?.let { n ->
            item {
                SectionTitle("Valeurs nutritionnelles (pour 100g)")
                Spacer(modifier = Modifier.height(8.dp))
                SectionCard {
                    NutrientRow("Énergie", "${n.energy.toInt()} kcal", isFirst = true)
                    NutrientRow("Énergie (kJ)", "${n.energyKj.toInt()} kJ")
                    NutrientRow("Matières grasses", "${n.fat.formatted()} g")
                    NutrientRow("dont Acides gras saturés", "${n.saturatedFat.formatted()} g", indented = true)
                    NutrientRow("Glucides", "${n.carbohydrates.formatted()} g")
                    NutrientRow("dont Sucres", "${n.sugar.formatted()} g", indented = true)
                    NutrientRow("Fibres alimentaires", "${n.fiber.formatted()} g")
                    NutrientRow("Protéines", "${n.protein.formatted()} g")
                    NutrientRow("Sel", "${n.salt.formatted()} g")
                    NutrientRow("Sodium", "${n.sodium.formatted()} g", isLast = true)
                }
            }
        }
    }
}


// --- Small helpers ---

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            content = content
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NutrientRow(
    label: String,
    value: String,
    indented: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    if (!isFirst) HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (indented) 16.dp else 0.dp,
                top = if (isFirst) 4.dp else 0.dp,
                bottom = if (isLast) 4.dp else 0.dp
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (indented)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun Double.formatted(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString()
    else "%.1f".format(this)
