package com.markemcallister.huntingtons.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markemcallister.huntingtons.BuildConfig
import com.markemcallister.huntingtons.data.Catalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    catalog: Catalog,
    refreshing: Boolean,
    onBack: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onRefresh: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("About & Disclaimer") },
                navigationIcon = { BackIconButton(onBack = onBack) },
                actions = {
                    IconButton(onClick = onOpenDisclaimer) {
                        Icon(Icons.Outlined.Gavel, contentDescription = "Disclaimer")
                    }
                    IconButton(onClick = onRefresh, enabled = !refreshing) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh latest research")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(text = "Purpose", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(text = catalog.about.purpose, style = MaterialTheme.typography.bodyLarge)
            }
            item {
                Text(text = "Medical disclaimer", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(text = catalog.disclaimer.fullText, style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = onOpenDisclaimer) { Text("Read the full disclaimer screen") }
            }
            item {
                Text(text = "How this app is sourced", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(text = catalog.about.sourcing, style = MaterialTheme.typography.bodyLarge)
            }
            item {
                Text(text = "Refresh behavior", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(text = catalog.about.refresh, style = MaterialTheme.typography.bodyLarge)
            }
            item {
                Text(text = "Version", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "App ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}). Content package ${catalog.about.version}. Content updated: ${formatContentDate(catalog.generatedAt)}.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
