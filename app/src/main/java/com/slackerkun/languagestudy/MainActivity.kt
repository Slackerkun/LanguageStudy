package com.slackerkun.languagestudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val situations = remember { RemoteDataSource.fetchSituations(this) }
            SituationalJapaneseUI(situations)
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SituationalJapaneseUI(situations: List<Situation>) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var expandedPhrase by remember { mutableStateOf<SituationalPhrase?>(null) }

    // existing toggle: which side to show first
    var showJapaneseFirst by remember { mutableStateOf(true) }
    // NEW: show/hide notes
    var showNotes by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Situational Japanese",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (situations.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No data found (remote + assets both failed)",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                // Row of controls (language + notes)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // language chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = showJapaneseFirst,
                            onClick = { showJapaneseFirst = true },
                            label = { Text("Japanese → English") }
                        )
                        FilterChip(
                            selected = !showJapaneseFirst,
                            onClick = { showJapaneseFirst = false },
                            label = { Text("English → Japanese") }
                        )
                    }

                    // notes toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Notes", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = showNotes,
                            onCheckedChange = { showNotes = it },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    situations.forEachIndexed { index, situation ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                                expandedPhrase = null
                            },
                            text = { Text(situation.title) }
                        )
                    }
                }

                val current = situations.getOrNull(selectedTabIndex)
                if (current != null) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(current.phrases) { phrase ->
                            PhraseCard(
                                phrase = phrase,
                                isExpanded = expandedPhrase == phrase,
                                onClick = {
                                    expandedPhrase =
                                        if (expandedPhrase == phrase) null else phrase
                                },
                                showJapaneseFirst = showJapaneseFirst,
                                showNotes = showNotes
                            )
                        }
                    }
                }

                // Footer banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Created by David Lambrix",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhraseCard(
    phrase: SituationalPhrase,
    isExpanded: Boolean,
    onClick: () -> Unit,
    showJapaneseFirst: Boolean,
    showNotes: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp)) {

            if (showJapaneseFirst) {
                // Japanese → English
                Text(
                    text = phrase.jp,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = phrase.romaji,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isExpanded) {
                    Spacer(Modifier.height(6.dp))
                    Text(phrase.en, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tap to show English",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // English → Japanese
                Text(
                    text = phrase.en,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = phrase.jp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isExpanded) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        phrase.romaji,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tap to show romaji",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // show note only if toggle ON and note exists
            if (showNotes) {
                phrase.note?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
