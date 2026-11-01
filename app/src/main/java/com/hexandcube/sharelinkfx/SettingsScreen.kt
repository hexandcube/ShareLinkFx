package com.hexandcube.sharelinkfx

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: SettingsRepository,
    onNavigateToHome: () -> Unit,
    onNavigateToEmbedServices: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val stripTracking by repository.stripTrackingFlow.collectAsState(initial = true)
    val stripOtherText by repository.stripOtherTextFlow.collectAsState(initial = false)
    val autoMastodon by repository.autoMastodonFlow.collectAsState(initial = false)
    val shareBehavior by repository.shareBehaviorFlow.collectAsState(initial = ShareBehavior.OPEN_SHARE_SHEET)
    var showBehaviorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_screen_title)) }
            )
        },
        bottomBar = {
            MainNavigationBar(
                currentRoute = "settings",
                onNavigateToHome = onNavigateToHome,
                onNavigateToSettings = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_item_embed_fixer_services_title)) },
                supportingContent = { Text(stringResource(R.string.settings_item_embed_fixer_services_text)) },
                modifier = Modifier.clickable { onNavigateToEmbedServices() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_item_share_behavior_title)) },
                supportingContent = { Text(stringResource(R.string.settings_item_share_behavior_text, stringResource(shareBehavior.labelResId))) },
                modifier = Modifier.clickable { showBehaviorDialog = true }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_item_strip_tracking_parameters_title)) },
                supportingContent = { Text(stringResource(R.string.settings_item_strip_tracking_parameters_text)) },
                trailingContent = {
                    Switch(
                        checked = stripTracking,
                        onCheckedChange = { checked -> coroutineScope.launch { repository.setStripTracking(checked) } }
                    )
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_item_strip_other_text_title)) },
                supportingContent = { Text(stringResource(R.string.settings_item_strip_other_text_text)) },
                trailingContent = {
                    Switch(
                        checked = stripOtherText,
                        onCheckedChange = { checked -> coroutineScope.launch { repository.setStripOtherText(checked) } }
                    )
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_item_automastodon_title)) },
                supportingContent = { Text(stringResource(R.string.settings_item_automastodon_text)) },
                trailingContent = {
                    Switch(
                        checked = autoMastodon,
                        onCheckedChange = { checked -> coroutineScope.launch { repository.setAutoMastodon(checked) } }
                    )
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_item_about_title)) },
                supportingContent = { Text(stringResource(R.string.settings_item_about_text)) },
                modifier = Modifier.clickable { onNavigateToAbout() }
            )
        }

        if (showBehaviorDialog) {
            AlertDialog(
                onDismissRequest = { showBehaviorDialog = false },
                title = { Text(stringResource(R.string.settings_dialog_share_bahavior_title)) },
                text = {
                    Column(Modifier.selectableGroup()) {
                        ShareBehavior.entries.forEach { behavior ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (behavior == shareBehavior),
                                        onClick = {
                                            coroutineScope.launch {
                                                repository.setShareBehavior(
                                                    behavior
                                                )
                                            }
                                            showBehaviorDialog = false
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (behavior == shareBehavior), onClick = null)
                                Text(stringResource(behavior.labelResId), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showBehaviorDialog = false }) { Text(
                    stringResource(R.string.action_cancel)
                ) } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbedServicesScreen(
    repository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val mastodonPrefix by repository.mastodonPrefixFlow.collectAsState(initial = ServiceConfig.MASTODON_DEFAULT_PREFIX)
    val customServices by repository.customServicesFlow.collectAsState(initial = emptyList())

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var serviceToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_services_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.settings_services_screen_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()

            ServiceConfig.platforms.sortedBy { it.name }.forEach { platform ->
                val currentValue by repository.getPlatformDomainFlow(platform.id).collectAsState(initial = platform.defaultTarget)

                ServiceConfigItem(
                    serviceName = platform.name,
                    currentValue = currentValue,
                    onValueChange = { newValue ->
                        coroutineScope.launch { repository.setPlatformDomain(platform.id, newValue) }
                    },
                    suggestions = platform.suggestions
                )
            }

            ServiceConfigItem(
                serviceName = stringResource(R.string.settings_services_item_mastodon_prefix),
                currentValue = mastodonPrefix,
                onValueChange = { coroutineScope.launch { repository.setMastodonPrefix(it) } },
                suggestions = ServiceConfig.mastodonSuggestions
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.settings_services_custom_services_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.settings_services_custom_services_text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showAddCustomDialog = true }) { Icon(Icons.Filled.Add, "Add Custom Service") }
            }

            if (customServices.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_services_custom_services_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                customServices.forEach { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(service.name, style = MaterialTheme.typography.titleMedium)
                                Text("${service.sourceDomain}  ➜  ${service.targetDomain}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { serviceToDelete = service.id }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showAddCustomDialog) {
            var serviceName by remember { mutableStateOf("") }
            var sourceDomain by remember { mutableStateOf("") }
            var targetDomain by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddCustomDialog = false },
                title = { Text(stringResource(R.string.settings_dialog_add_custom_service_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = serviceName, onValueChange = { serviceName = it }, label = { Text(
                            stringResource(R.string.settings_dialog_add_custom_service_field_label_service_name)
                        ) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = sourceDomain, onValueChange = { sourceDomain = it }, label = { Text(
                            stringResource(R.string.settings_dialog_add_custom_service_field_label_source_domain)
                        ) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = targetDomain, onValueChange = { targetDomain = it }, label = { Text(
                            stringResource(R.string.settings_dialog_add_custom_service_field_label_target_domain)
                        ) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (serviceName.isNotBlank() && sourceDomain.isNotBlank() && targetDomain.isNotBlank()) {
                                coroutineScope.launch { repository.addCustomService(serviceName.trim(), sourceDomain.trim(), targetDomain.trim()) }
                                showAddCustomDialog = false
                            }
                        }
                    ) { Text(stringResource(R.string.settings_dialog_add_custom_service_action_add)) }
                },
                dismissButton = { TextButton(onClick = { showAddCustomDialog = false }) { Text(
                    stringResource(R.string.action_cancel)
                ) } }
            )
        }

        if (serviceToDelete != null) {
            AlertDialog(
                onDismissRequest = { serviceToDelete = null },
                title = { Text(stringResource(R.string.settings_dialog_delete_rule_title)) },
                text = { Text(stringResource(R.string.settings_dialog_delete_rule_text)) },
                confirmButton = {
                    Button(onClick = {
                        coroutineScope.launch {
                            repository.deleteCustomService(serviceToDelete!!)
                            serviceToDelete = null
                        }
                    }) { Text(stringResource(R.string.settings_dialog_delete_rule_action_delete)) }
                },
                dismissButton = { TextButton(onClick = { serviceToDelete = null }) { Text(
                    stringResource(R.string.action_cancel)
                ) } }
            )
        }
    }
}

@Composable
fun ServiceConfigItem(
    serviceName: String,
    currentValue: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(serviceName, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentValue,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.settings_services_field_label_service_host_domain)) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                SuggestionChip(onClick = { onValueChange(suggestion) }, label = { Text(suggestion) })
            }
        }
    }
}