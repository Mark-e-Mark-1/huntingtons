package com.markemcallister.huntingtons.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.markemcallister.huntingtons.data.EducationSection
import com.markemcallister.huntingtons.data.NamedLink
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionCard(
    section: EducationSection,
    onOpenGlossary: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(text = section.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(text = section.body, style = MaterialTheme.typography.bodyLarge)
            if (section.glossaryTerms.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    section.glossaryTerms.take(4).forEach { term ->
                        AssistChip(
                            onClick = { onOpenGlossary(term) },
                            label = { Text("Glossary: $term") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalCallout(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

@Composable
fun SourceList(sources: List<NamedLink>, modifier: Modifier = Modifier) {
    if (sources.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Sources", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        sources.forEach { source ->
            OpenLinkButton(label = source.label, url = source.url)
        }
    }
}

@Composable
fun OpenLinkButton(label: String, url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    TextButton(
        onClick = { openExternalUrl(context, url) },
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

fun openExternalUrl(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No browser available; keep the app running.
    }
}

fun formatContentDate(raw: String): String {
    val parsedInstant = runCatching { Instant.parse(raw) }.getOrNull()
    if (parsedInstant != null) {
        val local = parsedInstant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        return local.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.US))
    }
    val parsedOffset = runCatching { OffsetDateTime.parse(raw) }.getOrNull()
    if (parsedOffset != null) {
        return parsedOffset.toLocalDate()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.US))
    }
    val parsedDate = runCatching { LocalDate.parse(raw.take(10)) }.getOrNull()
    if (parsedDate != null) {
        return parsedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.US))
    }
    return raw
}
