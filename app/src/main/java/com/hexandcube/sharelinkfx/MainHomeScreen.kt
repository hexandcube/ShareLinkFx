package com.hexandcube.sharelinkfx

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MainHomeScreen(
    onNavigateToSettings: () -> Unit,
    onManualFix: (String) -> Unit
) {
    var manualLink by remember { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        bottomBar = {
            MainNavigationBar(
                currentRoute = "home",
                onNavigateToHome = { },
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Icon",
                modifier = Modifier.size(160.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "ShareLinkFx",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.home_screen_hint_text),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.home_screen_or_divider),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = manualLink,
                onValueChange = { manualLink = it },
                label = { Text(stringResource(R.string.home_field_label_link)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (manualLink.isNotEmpty()) {
                        IconButton(onClick = { manualLink = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.home_action_description_clear_field))
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (manualLink.isNotBlank()) onManualFix(manualLink)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = manualLink.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoFixHigh,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp)
                )
                Text(stringResource(R.string.home_action_fix_link))
            }

            Spacer(modifier = Modifier.height(48.dp))

            val kofiUrl = stringResource(R.string.url_kofi)

            TextButton(onClick = { uriHandler.openUri(kofiUrl) }) {
                Icon(
                    imageVector = Icons.Filled.VolunteerActivism,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                Text(stringResource(R.string.action_support_kofi))
            }
        }
    }
}