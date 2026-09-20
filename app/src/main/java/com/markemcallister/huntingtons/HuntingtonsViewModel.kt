package com.markemcallister.huntingtons

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.markemcallister.huntingtons.data.Catalog
import com.markemcallister.huntingtons.data.ClinicalTrialsClient
import com.markemcallister.huntingtons.data.ContentLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HuntingtonsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _catalogState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val catalogState: StateFlow<CatalogUiState> = _catalogState.asStateFlow()

    private val _screen = MutableStateFlow(initialScreen())
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private var baseline: Catalog? = null

    private val crashGuard = CoroutineExceptionHandler { _, throwable ->
        reportFailure(throwable)
    }

    init {
        viewModelScope.launch(Dispatchers.IO + crashGuard) {
            try {
                val catalog = getApplication<Application>().assets.let { assets ->
                    ContentLoader.openAsset { name -> assets.open(name) }.use { stream ->
                        ContentLoader.load(stream)
                    }
                }
                val merged = applyCachedResearch(catalog)
                withContext(Dispatchers.Main.immediate) {
                    baseline = catalog
                    _catalogState.value = CatalogUiState.Ready(merged)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                reportFailure(failure)
            }
        }
    }

    fun acceptDisclaimer() {
        prefs.edit().putBoolean(KEY_DISCLAIMER, true).apply()
        _screen.value = Screen.Home
    }

    fun openDisclaimer() {
        _screen.value = Screen.Disclaimer
    }

    fun openHome() {
        _screen.value = Screen.Home
    }

    fun openOverview() {
        _screen.value = Screen.Overview
    }

    fun openTreatments() {
        _screen.value = Screen.Treatments
    }

    fun openResearch() {
        _screen.value = Screen.Research
    }

    fun openResources() {
        _screen.value = Screen.Resources
    }

    fun openGlossary(query: String = "") {
        _screen.value = Screen.Glossary(query)
    }

    fun openAbout() {
        _screen.value = Screen.About
    }

    fun goBack() {
        when (_screen.value) {
            Screen.Home -> { }
            Screen.Disclaimer -> {
                if (prefs.getBoolean(KEY_DISCLAIMER, false)) {
                    _screen.value = Screen.Home
                }
            }
            else -> _screen.value = Screen.Home
        }
    }

    fun refreshResearch() {
        if (_refreshing.value) return
        val current = (_catalogState.value as? CatalogUiState.Ready)?.catalog ?: return
        _refreshing.value = true
        viewModelScope.launch(Dispatchers.IO + crashGuard) {
            try {
                val items = ClinicalTrialsClient.fetchTrialItems()
                if (items.isEmpty()) {
                    throw IllegalStateException("ClinicalTrials.gov returned no HD studies")
                }
                val fetchedAt = ClinicalTrialsClient.nowIso()
                cacheFile().writeText(ContentLoader.writeResearchCache(fetchedAt, items), Charsets.UTF_8)
                val updated = current.copy(
                    generatedAt = fetchedAt,
                    research = current.research.copy(trialItems = items),
                )
                withContext(Dispatchers.Main.immediate) {
                    _catalogState.value = CatalogUiState.Ready(updated)
                    _refreshing.value = false
                    _messages.tryEmit("Latest research updated from ClinicalTrials.gov.")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                withContext(Dispatchers.Main.immediate) {
                    _refreshing.value = false
                    val detail = failure.message?.takeIf { it.isNotBlank() } ?: failure.javaClass.simpleName
                    _messages.tryEmit("Could not refresh. Showing last saved content. ($detail)")
                }
            }
        }
    }

    private fun initialScreen(): Screen {
        return if (prefs.getBoolean(KEY_DISCLAIMER, false)) Screen.Home else Screen.Disclaimer
    }

    private fun applyCachedResearch(catalog: Catalog): Catalog {
        val file = cacheFile()
        if (!file.exists()) return catalog
        return try {
            val (fetchedAt, items) = ContentLoader.parseResearchCache(file.readText(Charsets.UTF_8))
            if (items.isEmpty()) catalog
            else catalog.copy(
                generatedAt = fetchedAt,
                research = catalog.research.copy(trialItems = items),
            )
        } catch (_: Exception) {
            catalog
        }
    }

    private fun cacheFile(): File {
        return File(getApplication<Application>().filesDir, CACHE_NAME)
    }

    private fun reportFailure(failure: Throwable) {
        val message = failure.message?.takeIf { it.isNotBlank() }
            ?: failure.javaClass.simpleName
        _catalogState.value = CatalogUiState.Failed(
            "Could not load the educational content ($message). You can keep the app open; try reinstalling the APK.",
        )
    }

    companion object {
        private const val PREFS = "huntingtons"
        private const val KEY_DISCLAIMER = "disclaimer_accepted"
        private const val CACHE_NAME = "research_cache.json"
    }
}
