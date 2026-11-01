package com.hexandcube.sharelinkfx

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun MainNavigationBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text(stringResource(R.string.navbar_item_home_label)) },
            selected = currentRoute == "home",
            onClick = { if (currentRoute != "home") onNavigateToHome() }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text(stringResource(R.string.navbar_item_settings_label)) },
            selected = currentRoute == "settings",
            onClick = { if (currentRoute != "settings") onNavigateToSettings() }
        )
    }
}