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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
    val ctx = LocalContext.current

    var situations by remember { mutableStateOf<List<Situation>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showJapanese by remember { mutableStateOf(true) }
    var remoteStatus by remember { mutableStateOf<RemoteStatus?>(null) }

    var hiddenPhrases by remember { mutableStateOf<Set<String>>(emptySet()) }
    var hiddenCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    // two separate overlays
    var showCategoryManager by remember { mutableStateOf(false) }
    var showHiddenCards by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) {
            RemoteDataSource.fetchSituations(ctx)
        }
        situations = result.data
        remoteStatus = result.status

        // load persisted
        hiddenPhrases = getHiddenPhrases(ctx)
        hiddenCategories = getHiddenCategories(ctx)

        // pick first visible
        val firstVisible = situations.firstOrNull { !hiddenCategories.contains(it.id) }
        selectedCategoryId = firstVisible?.id
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HeaderBar_WithLogo(
            showJapanese = showJapanese,
            onToggle = { showJapanese = it },
            onManageCategories = { showCategoryManager = true },
            onShowHiddenCards = { showHiddenCards = true }
        )

        CategoryBar(
            situations = situations,
            selectedCategoryId = selectedCategoryId,
            hiddenCategories = hiddenCategories,
            onCategorySelected = { selectedCategoryId = it }
        )

        val selectedSituation = situations.firstOrNull { it.id == selectedCategoryId }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (selectedSituation == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No category selected or it is hidden.", color = Color.Gray)
                }
            } else {
                PhraseList(
                    situation = selectedSituation,
                    showJapanese = showJapanese,
                    hiddenPhrases = hiddenPhrases,
                    onHidePhrase = { phraseKey ->
                        addHiddenPhrase(ctx, phraseKey)
                        hiddenPhrases = getHiddenPhrases(ctx)
                    }
                )
            }
        }

        FooterBar_WithSource(remoteStatus)
    }

    // overlay 1: categories
    if (showCategoryManager) {
        CategoryManagerScreen(
            situations = situations,
            hiddenCategories = hiddenCategories,
            onUpdateHiddenCategories = { newSet ->
                saveHiddenCategories(ctx, newSet)
                hiddenCategories = getHiddenCategories(ctx)

                // reselect if current became hidden
                val selectedCategoryId = situations.firstOrNull { !hiddenCategories.contains(it.id) }?.id
                // update with key (we don't have direct state here, so we ignore)
            },
            onClose = { showCategoryManager = false }
        )
    }

    // overlay 2: hidden cards
    if (showHiddenCards) {
        HiddenCardsScreen(
            situations = situations,
            hiddenPhrases = hiddenPhrases,
            onUnhidePhrase = { key ->
                removeHiddenPhrase(ctx, key)
                hiddenPhrases = getHiddenPhrases(ctx)
            },
            onUnhideAll = {
                // clear only phrases? or both? — user said “separate from categories”, so we clear phrases only
                clearHiddenPhrases(ctx)
                hiddenPhrases = getHiddenPhrases(ctx)
                Toast.makeText(ctx, "All hidden cards restored", Toast.LENGTH_SHORT).show()
            },
            onClose = { showHiddenCards = false }
        )
    }
}

@Composable
fun HeaderBar_WithLogo(
    showJapanese: Boolean,
    onToggle: (Boolean) -> Unit,
    onManageCategories: () -> Unit,
    onShowHiddenCards: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00796B))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // MENU
        Box {
            Text(
                text = "☰",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable { menuExpanded = true }
            )

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Manage categories") },
                    onClick = {
                        onManageCategories()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hidden cards") },
                    onClick = {
                        onShowHiddenCards()
                        menuExpanded = false
                    }
                )
            }
        }

        // LOGO + TITLE
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_playstore),
                contentDescription = "Nihongo Life logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .padding(end = 8.dp)
            )
            Text(
                text = "Nihongo Life",
                color = Color.White,
                fontSize = 20.sp
            )
        }

        // JP / EN toggle
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
    hiddenCategories: Set<String>,
    onCategorySelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0F2F1))
            .horizontalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        situations.forEach { situation ->
            if (hiddenCategories.contains(situation.id)) return@forEach

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
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun PhraseList(
    situation: Situation?,
    showJapanese: Boolean,
    hiddenPhrases: Set<String>,
    onHidePhrase: (String) -> Unit
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

        val visiblePhrases = situation.phrases.filter { phrase ->
            val key = phraseKey(situation.id, phrase)
            !hiddenPhrases.contains(key)
        }

        if (visiblePhrases.isEmpty()) {
            Text(text = "All cards hidden for this category.", color = Color.Gray)
            return@Column
        }

        visiblePhrases.forEach { phrase ->
            var flipped by remember(phrase.jp) { mutableStateOf(false) }

            val showFront = if (flipped) !showJapanese else showJapanese
            val mainText = if (showFront) phrase.jp else phrase.en
            val key = phraseKey(situation.id, phrase)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { flipped = !flipped },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // label
                    Text(
                        text = if (showFront) "Japanese" else "English",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // actions: Hide (left) / Copy (right)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hide",
                            color = Color(0xFFB71C1C),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { onHidePhrase(key) }
                                .padding(4.dp)
                        )
                        Text(
                            text = "Copy",
                            color = Color(0xFF00796B),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(mainText))
                                    Toast
                                        .makeText(
                                            context,
                                            "Copied: $mainText",
                                            Toast.LENGTH_SHORT
                                        )
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

/* =========================
   CATEGORY MANAGER SCREEN
   ========================= */
@Composable
fun CategoryManagerScreen(
    situations: List<Situation>,
    hiddenCategories: Set<String>,
    onUpdateHiddenCategories: (Set<String>) -> Unit,
    onClose: () -> Unit
) {
    var localHiddenCategories by remember(hiddenCategories) { mutableStateOf(hiddenCategories) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            shape = MaterialTheme.shapes.large,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manage categories", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Close",
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.clickable {
                            onUpdateHiddenCategories(localHiddenCategories)
                            onClose()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Uncheck categories you don't want to see on the main screen.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    situations.forEach { situation ->
                        val isHidden = localHiddenCategories.contains(situation.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    localHiddenCategories =
                                        if (isHidden) localHiddenCategories - situation.id
                                        else localHiddenCategories + situation.id
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = !isHidden, // checked = visible
                                    onCheckedChange = { checked ->
                                        localHiddenCategories =
                                            if (checked) localHiddenCategories - situation.id
                                            else localHiddenCategories + situation.id
                                    }
                                )
                                Text(text = situation.title)
                            }
                            if (isHidden) {
                                Text("Hidden", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // show all
                            localHiddenCategories = emptySet()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show all categories")
                    }
                }
            }
        }
    }
}

/* =========================
   HIDDEN CARDS SCREEN
   ========================= */
@Composable
fun HiddenCardsScreen(
    situations: List<Situation>,
    hiddenPhrases: Set<String>,
    onUnhidePhrase: (String) -> Unit,
    onUnhideAll: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            shape = MaterialTheme.shapes.large,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hidden cards", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Close",
                        color = Color(0xFFB71C1C),
                        modifier = Modifier.clickable { onClose() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (hiddenPhrases.isEmpty()) {
                        Text("No hidden cards.", color = Color.Gray)
                    } else {
                        hiddenPhrases.forEach { key ->
                            val parts = key.split("|", limit = 2)
                            val situationId = parts.getOrNull(0)
                            val jp = parts.getOrNull(1) ?: ""
                            val situation = situations.firstOrNull { it.id == situationId }
                            val phrase = situation?.phrases?.firstOrNull { it.jp == jp }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = phrase?.jp ?: jp, fontWeight = FontWeight.Medium)
                                    if (phrase?.en?.isNotEmpty() == true)
                                        Text(text = phrase.en, fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = "Category: ${situation?.title ?: "(Unknown)"}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                TextButton(onClick = { onUnhidePhrase(key) }) {
                                    Text("Restore")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onUnhideAll()
                                onClose()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Restore ALL hidden cards")
                        }
                    }
                }
            }
        }
    }
}

/* =========================
   KEY HELPER
   ========================= */
private fun phraseKey(situationId: String, phrase: Phrase): String {
    return situationId + "|" + phrase.jp
}
