package com.hexandcube.sharelinkfx

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hexandcube.sharelinkfx.ui.theme.ShareLinkFxTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private val settingsRepository by lazy { SettingsRepository(this) }
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(settingsRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        var initialSharedText: String? = null
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            initialSharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        }

        setContent {
            ShareLinkFxTheme {
                val navController = rememberNavController()

                val errorPrompt by viewModel.errorPrompt.collectAsStateWithLifecycle()
                val mastodonPrompt by viewModel.mastodonPrompt.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.shareEvents.collect { event ->
                        when (event) {
                            is ShareEvent.FinalizeShare -> {
                                val behavior = settingsRepository.shareBehaviorFlow.first()
                                handleFixedLink(this@MainActivity, event.fixedText, behavior)
                                if (event.isFromIntent) finish()
                            }
                        }
                    }
                }

                LaunchedEffect(initialSharedText) {
                    initialSharedText?.let { viewModel.attemptFixLink(it, true) }
                }

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable("home") {
                        MainHomeScreen(
                            onNavigateToSettings = { navController.navigate("settings") { launchSingleTop = true } },
                            onManualFix = { manualLink -> viewModel.attemptFixLink(manualLink, false) }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            repository = settingsRepository,
                            onNavigateToHome = { navController.navigate("home") { launchSingleTop = true; popUpTo("home") } },
                            onNavigateToEmbedServices = { navController.navigate("embed_services") },
                            onNavigateToAbout = { navController.navigate("about") }
                        )
                    }
                    composable("embed_services") {
                        EmbedServicesScreen(
                            repository = settingsRepository,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("about") {
                        AboutScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }

                errorPrompt?.let { prompt ->
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.dismissError()
                            if (prompt.isFromIntent) finish()
                        },
                        title = { Text(stringResource(R.string.error_invalid_link_title)) },
                        text = { Text(stringResource(prompt.messageResId)) },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.dismissError()
                                if (prompt.isFromIntent) finish()
                            }) { Text(stringResource(R.string.action_ok)) }
                        }
                    )
                }

                mastodonPrompt?.let { prompt ->
                    var showAutoMastodonDialog by remember { mutableStateOf(false) }

                    fun abortShare() {
                        viewModel.dismissMastodonPrompt()
                        if (prompt.isFromIntent) finish()
                    }

                    if (showAutoMastodonDialog) {
                        AlertDialog(
                            onDismissRequest = { showAutoMastodonDialog = false },
                            title = { Text(stringResource(R.string.dialog_automastodon_title)) },
                            text = { Text(stringResource(R.string.dialog_enable_automastodon_text)) },
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.confirmMastodonShare(enableAutoMastodon = true)
                                    showAutoMastodonDialog = false
                                }) { Text(stringResource(R.string.dialog_enable_automastodon_action_confirm)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAutoMastodonDialog = false }) { Text(
                                    stringResource(R.string.action_cancel)
                                ) }
                            }
                        )
                    } else {
                        AlertDialog(
                            onDismissRequest = { abortShare() },
                            title = { Text(stringResource(R.string.dialog_unrecognized_link_title)) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(stringResource(R.string.dialog_unrecognized_link_text))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = { viewModel.confirmMastodonShare(enableAutoMastodon = false) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text(stringResource(R.string.dialog_unrecognized_link_action_continue)) }

                                    OutlinedButton(
                                        onClick = { showAutoMastodonDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text(stringResource(R.string.dialog_unrecognized_link_action_always_continue)) }

                                    OutlinedButton(
                                        onClick = { abortShare() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text(stringResource(R.string.action_cancel)) }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {}
                        )
                    }
                }
            }
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun handleFixedLink(context: Context, fixedText: String, behavior: ShareBehavior) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        when (behavior) {
            ShareBehavior.COPY_TO_CLIPBOARD -> {
                clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.clipboard_label_fixed_link), fixedText))
                Toast.makeText(context,
                    getString(R.string.toast_linked_fixed_and_copied), Toast.LENGTH_SHORT).show()
            }
            ShareBehavior.OPEN_SHARE_SHEET -> {
                reshareFixedLink(context, fixedText)
                Toast.makeText(context, getString(R.string.toast_link_fixed), Toast.LENGTH_SHORT).show()
            }
            ShareBehavior.BOTH -> {
                clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.clipboard_label_fixed_link), fixedText))
                reshareFixedLink(context, fixedText)
                Toast.makeText(context, getString(R.string.toast_linked_fixed_and_copied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reshareFixedLink(context: Context, fixedText: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, fixedText)
        }
        context.startActivity(Intent.createChooser(shareIntent,
            getString(R.string.intent_sharesheet_share_fixed_link_title)))
    }
}