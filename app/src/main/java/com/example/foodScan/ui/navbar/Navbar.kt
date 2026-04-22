package com.example.foodScan.ui.navbar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.foodScan.R

@Composable
fun MyBottomBar() {

    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf("", "Liste", "Scan")

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    when (index) {
                        0 -> Icon(
                            painter = painterResource(R.drawable.planet_icon),
                            contentDescription = item,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        1 -> Icon(
                            imageVector = if (selectedItem == index)
                                Icons.AutoMirrored.Filled.List
                            else
                                Icons.AutoMirrored.Outlined.List,
                            contentDescription = item
                        )
                        2 -> Icon(
                            imageVector = if (selectedItem == index)
                                Icons.Filled.Favorite
                            else
                                Icons.Outlined.FavoriteBorder,
                            contentDescription = item
                        )
                    }
                },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = { selectedItem = index }
            )
        }
    }
}