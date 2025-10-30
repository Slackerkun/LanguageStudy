package com.slackerkun.languagestudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // ⬇️ changed to use remote JSON, with fallback inside RemoteDataSource
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
            ) {
                // Tabs
                TabRow(selectedTabIndex = selectedTabIndex) {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(current.phrases) { phrase ->
                            PhraseCard(
                                phrase = phrase,
                                isExpanded = expandedPhrase == phrase,
                                onClick = {
                                    expandedPhrase =
                                        if (expandedPhrase == phrase) null else phrase
                                }
                            )
                        }
                    }
                }

                // Footer banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Created by David Lambrix",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun PhraseCard(
    phrase: SituationalPhrase,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = phrase.jp,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = phrase.romaji,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (isExpanded) {
                Spacer(Modifier.height(6.dp))
                Text(phrase.en, style = MaterialTheme.typography.bodyMedium)
                phrase.note?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tap to show English",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}