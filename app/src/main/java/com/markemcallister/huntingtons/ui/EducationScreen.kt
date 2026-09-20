package com.markemcallister.huntingtons.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.markemcallister.huntingtons.data.EducationPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(
    page: EducationPage,
    lastReviewed: String,
    disclaimer: String?,
    refreshing: Boolean,
    onBack: () -> Unit,
    onOpenGlossary: (String) -> Unit,
    onOpenDisclaimer: () -> Unit,
    onRefresh: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(page.title) },
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
            if (!disclaimer.isNullOrBlank()) {
                item { MedicalCallout(disclaimer) }
            }
            if (page.callout.isNotBlank()) {
                item { MedicalCallout(page.callout) }
            }
            if (page.intro.isNotBlank()) {
                item {
                    Text(text = page.intro, style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(page.sections, key = { it.id }) { section ->
                SectionCard(section = section, onOpenGlossary = onOpenGlossary)
            }
            item {
                SourceList(page.sources)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Education last reviewed: ${formatContentDate(lastReviewed)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { onOpenGlossary("") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Glossary")
                }
            }
        }
    }
}
