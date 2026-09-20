package com.markemcallister.huntingtons.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.markemcallister.huntingtons.data.ResearchItem
import com.markemcallister.huntingtons.data.ResearchPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    research: ResearchPage,
    disclaimer: String,
    contentUpdated: String,
    refreshing: Boolean,
    onBack: () -> Unit,
    onOpenGlossary: (String) -> Unit,
    onOpenDisclaimer: () -> Unit,
    onRefresh: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val datedCards = (research.notableDevelopments + research.trialItems)
        .sortedByDescending { it.date }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(research.title) },
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
            item { MedicalCallout(disclaimer) }
            item {
                Text(text = "How to read this page", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(text = research.howToRead, style = MaterialTheme.typography.bodyLarge)
            }
            item {
                Text(text = "Pipeline themes", style = MaterialTheme.typography.titleMedium)
            }
            items(research.pipelineThemes, key = { "theme-${it.id}" }) { theme ->
                SectionCard(section = theme, onOpenGlossary = onOpenGlossary)
            }
            item {
                Text(text = "Clinical trials primer", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(text = research.clinicalTrialsPrimer, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { openExternalUrl(context, research.exploreTrialsUrl) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(research.exploreTrialsLabel)
                }
                Spacer(Modifier.height(8.dp))
                MedicalCallout(research.askSpecialist)
            }
            item {
                Text(
                    text = "Dated records (newest first)",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Fetched or packaged: ${formatContentDate(contentUpdated)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(datedCards, key = { it.id }) { item ->
                ResearchCard(item)
            }
        }
    }
}

@Composable
private fun ResearchCard(item: ResearchItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatContentDate(item.date),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(text = item.summary, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Source: ${item.source}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.material3.TextButton(
                onClick = { openExternalUrl(context, item.sourceUrl) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                Spacer(Modifier.padding(start = 8.dp))
                Text("Open source")
            }
        }
    }
}
