package com.hexandcube.sharelinkfx

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.annotation.StringRes

val Context.dataStore by preferencesDataStore(name = "user_settings")

@Serializable
data class CustomService(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sourceDomain: String,
    val targetDomain: String
)

enum class ShareBehavior(@StringRes val labelResId: Int) {
    OPEN_SHARE_SHEET(R.string.settings_behavior_open_share_sheet),
    COPY_TO_CLIPBOARD(R.string.settings_behavior_copy_to_clipboard),
    BOTH(R.string.settings_behavior_both)
}

class SettingsRepository(private val context: Context) {

    companion object {
        val STRIP_TRACKING = booleanPreferencesKey("strip_tracking")
        val STRIP_OTHER_TEXT = booleanPreferencesKey("strip_other_text")
        val SHARE_BEHAVIOR = stringPreferencesKey("share_behavior")
        val AUTO_MASTODON = booleanPreferencesKey("auto_mastodon")
        val MASTODON_PREFIX = stringPreferencesKey("mastodon_prefix")
        val CUSTOM_SERVICES_JSON = stringPreferencesKey("custom_services_json")
    }


    val stripTrackingFlow: Flow<Boolean> = context.dataStore.data.map { it[STRIP_TRACKING] ?: true }
    val stripOtherTextFlow: Flow<Boolean> = context.dataStore.data.map { it[STRIP_OTHER_TEXT] ?: false }
    val autoMastodonFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_MASTODON] ?: false }
    val mastodonPrefixFlow: Flow<String> = context.dataStore.data.map { it[MASTODON_PREFIX] ?: ServiceConfig.MASTODON_DEFAULT_PREFIX }
    val shareBehaviorFlow: Flow<ShareBehavior> = context.dataStore.data.map { prefs ->
        runCatching { ShareBehavior.valueOf(prefs[SHARE_BEHAVIOR] ?: "") }.getOrDefault(ShareBehavior.OPEN_SHARE_SHEET)
    }

    fun getPlatformDomainFlow(platformId: String): Flow<String> {
        val default = ServiceConfig.platforms.find { it.id == platformId }?.defaultTarget ?: ""
        return context.dataStore.data.map { it[stringPreferencesKey("${platformId}_domain")] ?: default }
    }

    suspend fun getAllDomainReplacements(): Map<String, String> {
        val prefs = context.dataStore.data.first()
        val replacements = mutableMapOf<String, String>()

        ServiceConfig.platforms.forEach { platform ->
            val target = prefs[stringPreferencesKey("${platform.id}_domain")] ?: platform.defaultTarget
            platform.sourceDomains.forEach { src ->
                replacements[src] = target
            }
        }

        parseCustomServices(prefs[CUSTOM_SERVICES_JSON] ?: "[]").forEach { custom ->
            if (custom.sourceDomain.isNotBlank() && custom.targetDomain.isNotBlank()) {
                replacements[custom.sourceDomain] = custom.targetDomain
            }
        }
        return replacements
    }

    suspend fun setStripTracking(enabled: Boolean) { context.dataStore.edit { it[STRIP_TRACKING] = enabled } }
    suspend fun setStripOtherText(enabled: Boolean) { context.dataStore.edit { it[STRIP_OTHER_TEXT] = enabled } } // NEW
    suspend fun setAutoMastodon(enabled: Boolean) { context.dataStore.edit { it[AUTO_MASTODON] = enabled } }
    suspend fun setShareBehavior(behavior: ShareBehavior) { context.dataStore.edit { it[SHARE_BEHAVIOR] = behavior.name } }
    suspend fun setMastodonPrefix(prefix: String) { context.dataStore.edit { it[MASTODON_PREFIX] = prefix } }

    suspend fun setPlatformDomain(platformId: String, url: String) {
        context.dataStore.edit { it[stringPreferencesKey("${platformId}_domain")] = url }
    }

    // Custom Service Helpers
    val customServicesFlow: Flow<List<CustomService>> = context.dataStore.data.map { parseCustomServices(it[CUSTOM_SERVICES_JSON] ?: "[]") }

    suspend fun addCustomService(name: String, source: String, target: String) {
        context.dataStore.edit { prefs ->
            val currentList = parseCustomServices(prefs[CUSTOM_SERVICES_JSON] ?: "[]").toMutableList()
            currentList.add(CustomService(name = name, sourceDomain = source, targetDomain = target))
            prefs[CUSTOM_SERVICES_JSON] = serializeCustomServices(currentList)
        }
    }

    suspend fun deleteCustomService(id: String) {
        context.dataStore.edit { prefs ->
            val currentList = parseCustomServices(prefs[CUSTOM_SERVICES_JSON] ?: "[]").toMutableList()
            currentList.removeAll { it.id == id }
            prefs[CUSTOM_SERVICES_JSON] = serializeCustomServices(currentList)
        }
    }

    private fun parseCustomServices(jsonString: String): List<CustomService> {
        return runCatching {
            Json.decodeFromString<List<CustomService>>(jsonString)
        }.getOrDefault(emptyList())
    }

    private fun serializeCustomServices(services: List<CustomService>): String {
        return Json.encodeToString(services)
    }
}