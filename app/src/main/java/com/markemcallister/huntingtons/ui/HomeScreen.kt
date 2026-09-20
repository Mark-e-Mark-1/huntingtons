package com.markemcallister.huntingtons.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markemcallister.huntingtons.data.Catalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    catalog: Catalog,
    refreshing: Boolean,
    onOpenOverview: () -> Unit,
    onOpenTreatments: () -> Unit,
    onOpenResearch: () -> Unit,
    onOpenResources: () -> Unit,
    onOpenGlossary: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onRefresh: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Huntington's") },
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
                Text(
                    text = "Huntington's Disease",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "A plain-language companion for patients, families, and caregivers. Educational only — not a clinical tool.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onOpenOverview, modifier = Modifier.fillMaxWidth()) {
                    Text("What is Huntington’s Disease")
                }
                Button(onClick = onOpenTreatments, modifier = Modifier.fillMaxWidth()) {
                    Text("Current Treatments")
                }
                Button(onClick = onOpenResearch, modifier = Modifier.fillMaxWidth()) {
                    Text("Latest Research and Cutting Edge Treatments")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onOpenResources, modifier = Modifier.fillMaxWidth()) {
                    Text("Resources & Support")
                }
                OutlinedButton(onClick = onOpenGlossary, modifier = Modifier.fillMaxWidth()) {
                    Text("Glossary")
                }
                OutlinedButton(onClick = onOpenAbout, modifier = Modifier.fillMaxWidth()) {
                    Text("About & Disclaimer")
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Content updated: ${formatContentDate(catalog.generatedAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
