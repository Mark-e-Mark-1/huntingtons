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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.markemcallister.huntingtons.data.ResourceItem
import com.markemcallister.huntingtons.data.ResourcePage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    page: ResourcePage,
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
                title = { Text("Resources & Support") },
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
                Text(text = page.intro, style = MaterialTheme.typography.bodyLarge)
            }
            items(page.items, key = { it.url }) { item ->
                ResourceCard(item)
            }
        }
    }
}

@Composable
private fun ResourceCard(item: ResourceItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            if (item.category.isNotBlank()) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyLarge)
            TextButton(
                onClick = { openExternalUrl(context, item.url) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                Spacer(Modifier.padding(start = 8.dp))
                Text("Open in browser")
            }
        }
    }
}
