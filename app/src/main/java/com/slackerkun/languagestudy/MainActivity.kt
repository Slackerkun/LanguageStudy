package com.slackerkun.languagestudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slackerkun.languagestudy.ui.theme.LanguageStudyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageStudyTheme {
                val situations = remember { RemoteDataSource.fetchSituations(this) }
                SituationalJapaneseUI(situations)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SituationalJapaneseUI(situations: List<Situation>) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showJapaneseFirst by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            // Gradient Header wrapped in Surface (for elevation)
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎌 Situational Japanese",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    TextButton(
                        onClick = { showJapaneseFirst = !showJapaneseFirst },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White.copy(alpha = 0.95f)
                        )
                    ) {
                        Text(
                            text = if (showJapaneseFirst) "JP" else "EN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            ) {
                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    situations.forEachIndexed { index, situation ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    situation.title,
                                    fontWeight = if (selectedTabIndex == index)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
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
                                defaultShowJapaneseFirst = showJapaneseFirst
                            )
                        }
                    }
                }

                FooterBar()
            }
        }
    }
}

@Composable
fun FooterBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
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
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "• v1.0",   // ← hardcoded for now
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PhraseCard(
    phrase: Phrase,
    defaultShowJapaneseFirst: Boolean
) {
    // start rotated depending on global toggle
    var baseRotation by remember {
        mutableStateOf(if (defaultShowJapaneseFirst) 0f else 180f)
    }

    val animatedRotation by animateFloatAsState(
        targetValue = baseRotation,
        animationSpec = tween(durationMillis = 300),
        label = "phraseFlip"
    )

    fun flipCard() {
        baseRotation += 180f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { flipCard() }
            .padding(vertical = 4.dp)
            .graphicsLayer {
                cameraDistance = 12 * density
            }
    ) {
        val rotation = animatedRotation % 360f

        // Front = Japanese
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    rotationY = rotation
                    alpha = if (rotation <= 90f || rotation >= 270f) 1f else 0f
                },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = phrase.jp,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phrase.romaji,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Back = English
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    rotationY = rotation + 180f
                    alpha = if (rotation > 90f && rotation < 270f) 1f else 0f
                },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = phrase.en,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phrase.jp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
4