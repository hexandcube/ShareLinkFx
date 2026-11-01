package com.hexandcube.sharelinkfx

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Icon",
                modifier = Modifier.size(160.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.about_developed_by_hexandcube), style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                stringResource(R.string.about_screen_text),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            val githubUrl = stringResource(R.string.url_github)
            val kofiUrl = stringResource(R.string.url_kofi)

            OutlinedButton(
                onClick = { uriHandler.openUri(githubUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.about_action_view_source))
            }

            OutlinedButton(
                onClick = { context.startActivity(Intent(context, OssLicensesMenuActivity::class.java)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Balance, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.about_action_view_open_source_licenses))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { uriHandler.openUri(kofiUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.VolunteerActivism, contentDescription = null, modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp))
                Text(stringResource(R.string.action_support_kofi))
            }

            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}