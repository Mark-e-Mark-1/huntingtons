package com.markemcallister.huntingtons.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markemcallister.huntingtons.CatalogUiState
import com.markemcallister.huntingtons.Screen
import com.markemcallister.huntingtons.data.Catalog
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun HuntingtonsApp(
    catalogState: CatalogUiState,
    screen: Screen,
    refreshing: Boolean,
    messages: SharedFlow<String>,
    onAcceptDisclaimer: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenOverview: () -> Unit,
    onOpenTreatments: () -> Unit,
    onOpenResearch: () -> Unit,
    onOpenResources: () -> Unit,
    onOpenGlossary: (String) -> Unit,
    onOpenAbout: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val state = catalogState) {
        CatalogUiState.Loading -> LoadingScreen(modifier)
        is CatalogUiState.Failed -> ErrorScreen(message = state.message, modifier = modifier)
        is CatalogUiState.Ready -> ReadyApp(
            catalog = state.catalog,
            screen = screen,
            refreshing = refreshing,
            messages = messages,
            onAcceptDisclaimer = onAcceptDisclaimer,
            onOpenDisclaimer = onOpenDisclaimer,
            onOpenHome = onOpenHome,
            onOpenOverview = onOpenOverview,
            onOpenTreatments = onOpenTreatments,
            onOpenResearch = onOpenResearch,
            onOpenResources = onOpenResources,
            onOpenGlossary = onOpenGlossary,
            onOpenAbout = onOpenAbout,
            onRefresh = onRefresh,
            onBack = onBack,
            modifier = modifier,
        )
    }
}

@Composable
private fun ReadyApp(
    catalog: Catalog,
    screen: Screen,
    refreshing: Boolean,
    messages: SharedFlow<String>,
    onAcceptDisclaimer: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenOverview: () -> Unit,
    onOpenTreatments: () -> Unit,
    onOpenResearch: () -> Unit,
    onOpenResources: () -> Unit,
    onOpenGlossary: (String) -> Unit,
    onOpenAbout: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(messages) {
        messages.collect { text ->
            snackbarHostState.showSnackbar(text)
        }
    }

    val canGoHome = screen !is Screen.Home && screen !is Screen.Disclaimer
    if (canGoHome || screen is Screen.Disclaimer) {
        BackHandler(onBack = onBack)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (screen) {
            Screen.Disclaimer -> DisclaimerScreen(
                disclaimer = catalog.disclaimer,
                onAccept = onAcceptDisclaimer,
            )
            Screen.Home -> HomeScreen(
                catalog = catalog,
                refreshing = refreshing,
                onOpenOverview = onOpenOverview,
                onOpenTreatments = onOpenTreatments,
                onOpenResearch = onOpenResearch,
                onOpenResources = onOpenResources,
                onOpenGlossary = { onOpenGlossary("") },
                onOpenAbout = onOpenAbout,
                onOpenDisclaimer = onOpenDisclaimer,
                onRefresh = onRefresh,
                snackbarHostState = snackbarHostState,
            )
            Screen.Overview -> EducationScreen(
                page = catalog.overview,
                lastReviewed = catalog.educationLastReviewed,
                disclaimer = null,
                refreshing = refreshing,
                onBack = onBack,
                onOpenGlossary = onOpenGlossary,
                onOpenDisclaimer = onOpenDisclaimer,
                onRefresh = onRefresh,
                snackbarHostState = snackbarHostState,
            )
            Screen.Treatments -> EducationScreen(
                page = catalog.treatments,
                lastReviewed = catalog.educationLastReviewed,
                disclaimer = catalog.disclaimer.shortText,
                refreshing = refreshing,
                onBack = onBack,
                onOpenGlossary = onOpenGlossary,
                onOpenDisclaimer = onOpenDisclaimer,
                onRefresh = onRefresh,
                snackbarHostState = snackbarHostState,
            )
            Screen.Research -> ResearchScreen(
                research = catalog.research,
                disclaimer = catalog.disclaimer.shortText,
                contentUpdated = catalog.generatedAt,
                refreshing = refreshing,
                onBack = onBack,
                onOpenGlossary = onOpenGlossary,
                onOpenDisclaimer = onOpenDisclaimer,
                onRefresh = onRefresh,
                snackbarHostState = snackbarHostState,
            )
            Screen.Resources -> ResourcesScreen(
                page = catalog.resources,
                refreshing = refreshing,
                onBack = onBack,
                onOpenDisclaimer = onOpenDisclaimer,
                onRefresh = onRefresh,
                snackbarHostState = snackbarHostState,
            )
            is Screen.Glossary -> GlossaryScreen(
                terms = catalog.glossary,
                initialQuery = screen.initialQuery,
                refreshing = refreshing,
                onBack = onBack,
                onOpenDisclaimer = onOpenDisclaimer,
                onRefresh = onRefresh,
                snackbarHostState = snackbarHostState,
            )
            Screen.About -> AboutScreen(
                catalog = catalog,
                refreshing = refreshing,
                onBack = onBack,
                onOpenDisclaimer = onOpenDisclaimer,
                onRefresh = onRefresh,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Huntington's") }) },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            Text(
                text = "Loading Huntington’s disease guide…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(
    message: String,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Huntington's") }) },
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) },
    ) { innerPadding ->
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
        )
    }
}
