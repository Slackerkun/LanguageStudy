package com.slackerkun.languagestudy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slackerkun.languagestudy.ui.theme.LanguageStudyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LanguageStudyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NihongoLifeScreen()
                }
            }
        }
    }
}

@Composable
fun NihongoLifeScreen() {
    val ctx = androidx.compose.ui.platform.LocalContext.current

    var situations by remember { mutableStateOf<List<Situation>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showJapanese by remember { mutableStateOf(true) }
    var remoteStatus by remember { mutableStateOf<RemoteStatus?>(null) }

    // initial load
    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) {
            RemoteDataSource.fetchSituations(ctx)
        }
        situations = result.data
        remoteStatus = result.status
        if (situations.isNotEmpty()) {
            selectedCategoryId = situations.first().id
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // HEADER (Logo + JP/EN toggle only)
        HeaderBar_WithLogo(
            showJapanese = showJapanese,
            onToggle = { showJapanese = it }
        )

        // CATEGORY BAR
        CategoryBar(
            situations = situations,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { selectedCategoryId = it }
        )

        // MAIN content
        val selectedSituation = situations.firstOrNull { it.id == selectedCategoryId }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            PhraseList(
                situation = selectedSituation,
                showJapanese = showJapanese
            )
        }

        // FOOTER
        FooterBar_WithSource(remoteStatus)
    }
}

@Composable
fun HeaderBar_WithLogo(
    showJapanese: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00796B))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 👇 logo + title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_playstore),
                contentDescription = "Nihongo Life logo",
                modifier = Modifier
                    .size(36.dp)                 // ⬆️ slightly larger (10%)
                    .clip(CircleShape)           // ⭕ circular
                    .padding(end = 8.dp)
            )
            Text(
                text = "Nihongo Life",
                color = Color.White,
                fontSize = 20.sp
            )
        }

        // JP / EN toggle only
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (showJapanese) "JP" else "EN",
                color = Color.White,
                modifier = Modifier.padding(end = 4.dp)
            )
            Switch(
                checked = showJapanese,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF004D40),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFF004D40),
                )
            )
        }
    }
}

@Composable
fun CategoryBar(
    situations: List<Situation>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0F2F1))
            .horizontalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        situations.forEach { situation ->
            val isSelected = situation.id == selectedCategoryId
            val bg = if (isSelected) Color(0xFF004D40) else Color.White
            val fg = if (isSelected) Color.White else Color(0xFF004D40)

            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(bg, shape = MaterialTheme.shapes.small)
                    .clickable { onCategorySelected(situation.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = situation.title,
                    color = fg,
                    fontWeight = if (isSelected)
                        FontWeight.Bold
                    else
                        FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun PhraseList(
    situation: Situation?,
    showJapanese: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        if (situation == null) {
            Text(text = "No phrases yet.", color = Color.Gray)
            return@Column
        }

        situation.phrases.forEach { phrase ->
            var flipped by remember(phrase.jp) { mutableStateOf(false) }

            // what we show now
            val showFront = if (flipped) !showJapanese else showJapanese
            val mainText = if (showFront) phrase.jp else phrase.en

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    // tap body = flip
                    .clickable { flipped = !flipped },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // header row with copy button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showFront) "Japanese" else "English",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "Copy",
                            color = Color(0xFF00796B),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(mainText))
                                    Toast
                                        .makeText(context, "Copied: $mainText", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (showFront) {
                        Text(text = phrase.jp, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = phrase.romaji, fontSize = 14.sp, color = Color.Gray)
                    } else {
                        Text(text = phrase.en, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = phrase.jp, fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun FooterBar_WithSource(remoteStatus: RemoteStatus?) {
    val statusText = when {
        remoteStatus == null -> "Source: loading..."
        remoteStatus.ok && remoteStatus.code == 200 -> "Source: ${remoteStatus.source} ✅ (${remoteStatus.code})"
        else -> "Source: ${remoteStatus.source} ❌ (${remoteStatus.code ?: "?"})"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF004D40))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Created by David Lambrix • v1.0",
            color = Color.White,
            fontSize = 12.sp
        )
        Text(
            text = statusText,
            color = Color(0xFFE0F2F1),
            fontSize = 12.sp
        )
    }
}
