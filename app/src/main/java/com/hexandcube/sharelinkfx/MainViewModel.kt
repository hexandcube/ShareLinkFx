package com.hexandcube.sharelinkfx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.annotation.StringRes

val URL_REGEX = "(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])".toRegex()

data class ErrorState(@StringRes val messageResId: Int, val isFromIntent: Boolean)
data class MastodonState(val url: String, val isFromIntent: Boolean)

sealed class ShareEvent {
    data class FinalizeShare(val fixedText: String, val isFromIntent: Boolean) : ShareEvent()
}

class MainViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _errorPrompt = MutableStateFlow<ErrorState?>(null)
    val errorPrompt: StateFlow<ErrorState?> = _errorPrompt.asStateFlow()

    private val _mastodonPrompt = MutableStateFlow<MastodonState?>(null)
    val mastodonPrompt: StateFlow<MastodonState?> = _mastodonPrompt.asStateFlow()

    private val _shareEvents = MutableSharedFlow<ShareEvent>()
    val shareEvents: SharedFlow<ShareEvent> = _shareEvents.asSharedFlow()

    fun attemptFixLink(rawText: String, isIntent: Boolean) {
        if (!URL_REGEX.containsMatchIn(rawText)) {
            _errorPrompt.value = ErrorState(R.string.error_invalid_link_text_not_found, isFromIntent = isIntent)
            return
        }

        viewModelScope.launch {
            val stripTracking = repository.stripTrackingFlow.first()
            val stripOtherText = repository.stripOtherTextFlow.first()
            val replacements = repository.getAllDomainReplacements()

            val (fixedText, wasDomainReplaced) = fixLink(rawText, stripTracking, stripOtherText, replacements)

            if (wasDomainReplaced) {
                _shareEvents.emit(ShareEvent.FinalizeShare(fixedText, isIntent))
            } else {
                val autoMastodon = repository.autoMastodonFlow.first()
                if (autoMastodon) {
                    val prefix = repository.mastodonPrefixFlow.first()
                    val fixedMastodon = applyMastodonPrefix(fixedText, prefix)
                    _shareEvents.emit(ShareEvent.FinalizeShare(fixedMastodon, isIntent))
                } else {
                    _mastodonPrompt.value = MastodonState(fixedText, isIntent)
                }
            }
        }
    }

    fun confirmMastodonShare(enableAutoMastodon: Boolean) {
        val prompt = _mastodonPrompt.value ?: return
        viewModelScope.launch {
            if (enableAutoMastodon) {
                repository.setAutoMastodon(true)
            }
            val prefix = repository.mastodonPrefixFlow.first()
            val fixedMastodon = applyMastodonPrefix(prompt.url, prefix)

            _shareEvents.emit(ShareEvent.FinalizeShare(fixedMastodon, prompt.isFromIntent))
            dismissMastodonPrompt()
        }
    }

    fun dismissError() { _errorPrompt.value = null }
    fun dismissMastodonPrompt() { _mastodonPrompt.value = null }

    private fun applyMastodonPrefix(text: String, prefix: String): String {
        return URL_REGEX.replace(text) { matchResult ->
            val originalUrl = matchResult.value
            val protocol = if (originalUrl.startsWith("http://", ignoreCase = true)) "http://" else "https://"
            val cleanUrl = originalUrl.removePrefix("https://").removePrefix("http://")
            val cleanPrefix = prefix.removePrefix("https://").removePrefix("http://").trimEnd('/')
            "$protocol$cleanPrefix/$cleanUrl"
        }
    }

    private fun fixLink(
        originalText: String,
        stripTracking: Boolean,
        stripOtherText: Boolean,
        domainReplacements: Map<String, String>
    ): Pair<String, Boolean> {
        var domainWasReplaced = false

        fun processUrl(matchedUrl: String): String {
            var url = matchedUrl
            if (stripTracking && url.contains("?")) {
                url = url.substringBefore("?")
            }
            for ((sourceDomain, replacement) in domainReplacements) {
                val cleanSource = Regex.escape(
                    sourceDomain.removePrefix("https://").removePrefix("http://").removePrefix("www.").trim('/')
                )
                val cleanTarget = replacement.removePrefix("https://").removePrefix("http://").trim('/')

                val regex = "(https?://)(?:www\\.)?$cleanSource".toRegex(RegexOption.IGNORE_CASE)
                if (regex.containsMatchIn(url)) {
                    url = url.replaceFirst(regex, "$1$cleanTarget")
                    domainWasReplaced = true
                    break
                }
            }
            return url
        }

        val newText = if (stripOtherText) {
            URL_REGEX.findAll(originalText).map { processUrl(it.value) }.joinToString("\n")
        } else {
            URL_REGEX.replace(originalText) { processUrl(it.value) }
        }

        return Pair(newText, domainWasReplaced)
    }
}

class MainViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}