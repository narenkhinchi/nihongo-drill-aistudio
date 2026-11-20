package com.nihongo.vocabdrill.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.nihongo.vocabdrill.model.ThemeMode
import com.nihongo.vocabdrill.viewmodel.ChapterFilter
import com.nihongo.vocabdrill.viewmodel.NihongoViewModel

@Composable
fun ChapterSelectScreen(viewModel: NihongoViewModel, onStartQuiz: (Int) -> Unit, onOpenKanji: () -> Unit = {}) {
    val library by viewModel.lessonLibrary.collectAsState()
    LaunchedEffect(Unit) { viewModel.refreshLessons() }
    val lessons by viewModel.filteredLessons.collectAsState()
    val search by viewModel.searchQuery.collectAsState()
    val filter by viewModel.chapterFilter.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val progress by viewModel.userProgress.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inset ->
        Box(Modifier.fillMaxSize().padding(inset), contentAlignment = Alignment.TopCenter) {
            LazyColumn(state = listState, modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(), contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("あ", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Nihongo Drill", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Your book · Your pace", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.updateThemeMode(if (settings.themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK) }, modifier = Modifier.testTag("btn_toggle_theme_quick")) {
                            Icon(if (settings.themeMode == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                                if (settings.themeMode == ThemeMode.DARK) "Switch to light mode" else "Switch to night mode")
                        }
                        IconButton(onClick = viewModel::openSettings, modifier = Modifier.testTag("btn_open_settings")) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val vocabInteractions = remember { MutableInteractionSource() }
                        val vocabHovered by vocabInteractions.collectIsHoveredAsState()
                        Surface(
                            modifier = Modifier.weight(1f).height(128.dp)
                                .clickable(interactionSource = vocabInteractions, indication = null) {
                                    scope.launch { listState.animateScrollToItem(if (settings.hideOtherLessons) 5 else 6) }
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = if (vocabHovered) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (vocabHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                val bookColor = MaterialTheme.colorScheme.primary
                                Canvas(modifier = Modifier.size(36.dp)) {
                                    val book = Path().apply {
                                        moveTo(size.width * 0.5f, size.height * 0.18f)
                                        cubicTo(size.width * 0.34f, size.height * 0.08f, size.width * 0.16f, size.height * 0.08f, size.width * 0.06f, size.height * 0.18f)
                                        lineTo(size.width * 0.06f, size.height * 0.78f)
                                        cubicTo(size.width * 0.22f, size.height * 0.68f, size.width * 0.38f, size.height * 0.72f, size.width * 0.5f, size.height * 0.84f)
                                        cubicTo(size.width * 0.62f, size.height * 0.72f, size.width * 0.78f, size.height * 0.68f, size.width * 0.94f, size.height * 0.78f)
                                        lineTo(size.width * 0.94f, size.height * 0.18f)
                                        cubicTo(size.width * 0.84f, size.height * 0.08f, size.width * 0.66f, size.height * 0.08f, size.width * 0.5f, size.height * 0.18f)
                                        close()
                                    }
                                    drawPath(book, color = bookColor, style = Stroke(width = 2.4.dp.toPx()))
                                    drawLine(bookColor,
                                        androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.18f),
                                        androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.84f),
                                        strokeWidth = 2.4.dp.toPx())
                                    for (line in 0..2) {
                                        val y = size.height * (0.34f + line * 0.14f)
                                        drawLine(bookColor,
                                            androidx.compose.ui.geometry.Offset(size.width * 0.16f, y),
                                            androidx.compose.ui.geometry.Offset(size.width * 0.42f, y),
                                            strokeWidth = 1.8.dp.toPx())
                                        drawLine(bookColor,
                                            androidx.compose.ui.geometry.Offset(size.width * 0.58f, y),
                                            androidx.compose.ui.geometry.Offset(size.width * 0.84f, y),
                                            strokeWidth = 1.8.dp.toPx())
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text("Vocab", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Learn vocabulary", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        val kanjiInteractions = remember { MutableInteractionSource() }
                        val kanjiHovered by kanjiInteractions.collectIsHoveredAsState()
                        Surface(
                            modifier = Modifier.weight(1f).height(128.dp)
                                .clickable(interactionSource = kanjiInteractions, indication = null, onClick = onOpenKanji),
                            shape = RoundedCornerShape(18.dp),
                            color = if (kanjiHovered) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (kanjiHovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("漢", color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(2.dp))
                                Text("Kanji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Learn kanji", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Completed" to "${progress.completedLessons.count { number -> library.catalog.any { it.number == number } }}/${library.catalog.size}", "Accuracy" to "${progress.overallAccuracy}%",
                                "Practiced" to "${progress.totalPracticedQuestions}").forEach { (label, value) ->
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(label, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(start = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            BasicTextField(search, viewModel::setSearchQuery, singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.weight(1f).padding(vertical = 12.dp).testTag("search_lessons_input"),
                                decorationBox = { field -> Box { if (search.isEmpty()) Text("Search lessons", style = MaterialTheme.typography.bodyMedium, color = Color.Gray); field() } })
                            if (search.isNotEmpty()) IconButton(onClick = { viewModel.setSearchQuery("") }) { Icon(Icons.Default.Clear, "Clear search") }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (settings.hideOtherLessons) "Showing lesson ${settings.currentLessonNumber}" else "All lessons visible",
                            Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        IconToggleButton(checked = settings.hideOtherLessons, onCheckedChange = { viewModel.toggleOtherLessons() }, modifier = Modifier.testTag("toggle_other_lessons")) {
                            Icon(if (settings.hideOtherLessons) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                if (settings.hideOtherLessons) "Show all lessons" else "Hide other lessons")
                        }
                    }
                }
                if (!settings.hideOtherLessons) item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChapterFilter.entries.forEach { choice -> FilterChip(selected = filter == choice,
                            onClick = { viewModel.setChapterFilter(choice) }, label = { Text(if (choice == ChapterFilter.ALL) "All (${library.catalog.size})" else choice.label) }) }
                    }
                }
                if (filter == ChapterFilter.FAVORITES) item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${progress.favoriteWordIds.size} starred words", style = MaterialTheme.typography.titleMedium)
                        if (progress.favoriteWordIds.isEmpty()) Text("Star words in a lesson’s vocabulary to practise them here.", style = MaterialTheme.typography.bodyMedium)
                        else Button(onClick = viewModel::startFavoriteWordsDrill, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("btn_drill_starred_words")) { Text("Practice starred") }
                    }
                } else item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Lessons", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.refreshLessons(true) }, enabled = !library.refreshing,
                            modifier = Modifier.testTag("btn_refresh_lessons")) {
                            if (library.refreshing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Refresh, "Check for new lessons")
                        }
                        TextButton(onClick = viewModel::startQuickRandomDrill, modifier = Modifier.testTag("btn_quick_random_drill")) { Text("Quick drill") }
                    }
                }
                library.refreshError?.let { message -> item {
                    Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } }
                items(lessons, key = { it.lessonNumber }) { lesson ->
                    val wrongCount = viewModel.wordsForLesson(lesson.lessonNumber).count { it.id in progress.wrongWordIds }
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(if (lesson.vocabCount > 0) "${lesson.vocabCount} words" + (progress.lessonScores[lesson.lessonNumber]?.let { " · Best $it%" } ?: "") else "Waiting for book vocabulary", style = MaterialTheme.typography.bodySmall)
                            val downloading = lesson.lessonNumber in library.downloading
                            val downloaded = library.isDownloaded(lesson.lessonNumber)
                            if (!downloaded || library.hasUpdate(lesson.lessonNumber)) {
                                OutlinedButton(onClick = { viewModel.downloadLesson(lesson.lessonNumber) },
                                    enabled = !downloading,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("btn_download_${lesson.lessonNumber}")) {
                                    if (downloading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                    else Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (downloading) "Downloading…" else if (downloaded) "Update lesson" else "Download")
                                }
                            }
                            library.errors[lesson.lessonNumber]?.let { message ->
                                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            if (downloaded) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(onClick = { viewModel.openPreview(lesson) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("btn_preview_${lesson.lessonNumber}")) { Text("Vocab") }
                                    Button(onClick = { onStartQuiz(lesson.lessonNumber) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("btn_drill_${lesson.lessonNumber}")) { Text("Start drill") }
                                }
                                if (wrongCount > 0) TextButton(onClick = { viewModel.startSavedMistakes(lesson.lessonNumber) }, modifier = Modifier.testTag("retry_mistakes_${lesson.lessonNumber}")) {
                                    Icon(Icons.Default.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Retry mistakes ($wrongCount)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
