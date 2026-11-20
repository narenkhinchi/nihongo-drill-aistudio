package com.nihongo.vocabdrill.ui.screens

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.platform.LocalDensity
import com.nihongo.vocabdrill.model.QuizQuestion
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nihongo.vocabdrill.viewmodel.NihongoViewModel

private val QuizPurple = Color(0xFF5034FF)
private val QuizGreen = Color(0xFF009D73)
private val QuizOrange = Color(0xFFFF9900)

@Composable
fun QuizScreen(lessonNumber: Int, viewModel: NihongoViewModel, customTitle: String? = null) {
    val questions by viewModel.currentQuizQuestions.collectAsState()
    val index by viewModel.currentQuestionIndex.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    val encouragement by viewModel.encouragementRemaining.collectAsState()
    val timerSeconds by viewModel.recallTimerSeconds.collectAsState()
    val timerRemaining by viewModel.recallRemainingMillis.collectAsState()
    val timerPaused by viewModel.recallTimerPaused.collectAsState()
    val scroll = rememberScrollState()
    LaunchedEffect(index) { scroll.scrollTo(0) }
    val question = questions.getOrNull(index) ?: return
    val revealed = question.meaningRevealed
    var confirmQuit by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.setQuizForeground(false)
            if (event == Lifecycle.Event.ON_RESUME) viewModel.setQuizForeground(true)
        }
        viewModel.setQuizForeground(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); viewModel.setQuizForeground(false) }
    }

    BackHandler { viewModel.pauseAutoAdvance(); confirmQuit = true }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = viewModel::previousQuestion, enabled = index > 0 && encouragement == null,
                            modifier = Modifier.heightIn(min = 48.dp).testTag("btn_previous_question")) { Text("← Prev") }
                        Button(onClick = viewModel::nextQuestion, enabled = encouragement == null && (settings.hideMcqOptions || question.isAnswered),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.heightIn(min = 48.dp).testTag("btn_next_question")) {
                            Text(if (index == questions.lastIndex) "Finish" else "Next →")
                        }
                    }
                }
    }) { inset ->
        Box(Modifier.fillMaxSize().padding(inset), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 800.dp).fillMaxWidth().verticalScroll(scroll).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { viewModel.pauseAutoAdvance(); confirmQuit = true }) {
                        Icon(Icons.Default.ArrowBack, null, Modifier.size(16.dp))
                        Text("Lessons")
                    }
                    Text(if (lessonNumber > 0) "CHAPTER $lessonNumber" else "REVIEW",
                        Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = viewModel::openSettings) { Icon(Icons.Default.Settings, "Quiz settings") }
                }
                LinearProgressIndicator(progress = { (index + 1f) / questions.size },
                    modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(customTitle ?: "Lesson $lessonNumber", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text("${index + 1} of ${questions.size}", style = MaterialTheme.typography.bodySmall)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { viewModel.toggleHideMcqOptions(!settings.hideMcqOptions) }) {
                        Icon(if (settings.hideMcqOptions) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (settings.hideMcqOptions) "Options Hidden" else "Hide Options")
                    }
                    Text("✓ ${questions.count { it.isCorrect }}   × ${questions.count { it.isAnswered && !it.isCorrect }}",
                        color = QuizGreen, style = MaterialTheme.typography.labelLarge)
                }
                if (settings.hideMcqOptions && timerSeconds == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Timer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(3, 5).forEach { seconds ->
                                OutlinedButton(onClick = { viewModel.selectRecallTimer(seconds) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("select_timer_$seconds")) {
                                    Text("${seconds}s")
                                }
                            }
                        }
                    }
                }
                Card(Modifier.fillMaxWidth().testTag("card_question_prompt"), shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        if (settings.hideMcqOptions && timerSeconds != null) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                Text(if (question.isAnswered) "Timer stopped" else "${((timerRemaining + 999) / 1000).coerceAtLeast(1)}s",
                                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("recall_countdown"))
                                IconButton(onClick = viewModel::toggleRecallTimerPause, enabled = !question.isAnswered,
                                    modifier = Modifier.testTag("toggle_timer_pause")) {
                                    Icon(if (timerPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        if (timerPaused) "Resume timer" else "Pause timer")
                                }
                            }
                        }
                        Text(if (question.promptText == question.vocabItem.english) "What is this word in Japanese?"
                            else "What does this Japanese word mean?", style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ReadableQuizText(question.promptText, Modifier.testTag("text_prompt_main"))
                        if (settings.showRomaji && !question.promptSubtext.isNullOrBlank()) {
                            Text("[${question.promptSubtext}]", color = MaterialTheme.colorScheme.primary, modifier = Modifier.testTag("text_prompt_subtext"))
                        }
                        TextButton(onClick = { viewModel.speak(question.vocabItem.kana) }) {
                            Icon(Icons.Default.VolumeUp, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp)); Text("Listen Pronunciation")
                        }
                    }
                }
                if (settings.hideMcqOptions) {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Know it? Tap Next. Check Meaning counts as wrong.", textAlign = TextAlign.Center)
                            if (revealed) {
                                // Never render distractors in recall mode.
                                ReadableQuizText(question.correctAnswer, Modifier.testTag("text_revealed_meaning"), maximumSize = 30, minimumSize = 24)
                                Text("Added to mistakes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            } else Button(onClick = { viewModel.revealMeaning(index) }, colors = ButtonDefaults.buttonColors(containerColor = QuizOrange),
                                shape = RoundedCornerShape(14.dp), modifier = Modifier.testTag("btn_check_meaning")) {
                                Icon(Icons.Default.Visibility, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp)); Text("Check Meaning")
                            }
                        }
                    }
                } else {
                    AnswerOptions(question, viewModel::submitAnswer)
                    if (question.isAnswered) Text(if (question.isCorrect) "✓ Correct! Well done." else "Correct answer: ${question.correctAnswer}",
                        color = if (question.isCorrect) QuizGreen else MaterialTheme.colorScheme.error)
                }

            }
        }
    }
    encouragement?.let { remaining ->
        EncouragementScreen(remaining, viewModel::continueAfterEncouragement)
    }
    if (confirmQuit) AlertDialog(onDismissRequest = { confirmQuit = false }, title = { Text("Leave this drill?") },
        text = { Text("Your mistakes are saved for retry. This unfinished drill will not count as a completed chapter.") },
        confirmButton = { TextButton(onClick = { confirmQuit = false; viewModel.quitQuiz() }) { Text("Leave") } },
        dismissButton = { TextButton(onClick = { confirmQuit = false }) { Text("Continue") } })
}

@Composable
internal fun AnswerOptions(question: QuizQuestion, onAnswer: (String) -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 340.dp || fontScale > 1.2f || question.options.any { it.length > 26 }) 1 else 2
        Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            question.options.chunked(columns).forEachIndexed { row, options ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEachIndexed { column, option ->
                        val correct = question.isAnswered && option == question.correctAnswer
                        val wrong = question.isAnswered && option == question.selectedAnswer && !correct
                        val faded = question.isAnswered && !correct && !wrong
                        val accent = if (correct) QuizGreen else if (wrong) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                        Card(Modifier.weight(1f).fillMaxHeight().heightIn(min = 72.dp).alpha(if (faded) 0.45f else 1f)
                            .clickable(enabled = !question.isAnswered) { onAnswer(option) }.testTag("option_button_${row * columns + column}"),
                            shape = RoundedCornerShape(18.dp), border = BorderStroke(if (correct || wrong) 2.dp else 1.dp, accent),
                            colors = CardDefaults.cardColors(containerColor = if (correct) MaterialTheme.colorScheme.tertiaryContainer else if (wrong) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(8.dp), color = if (correct || wrong) accent else MaterialTheme.colorScheme.surfaceVariant) {
                                    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                        Text("${row * columns + column + 1}", color = if (correct || wrong) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(option, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(6.dp))
                                Box(Modifier.size(18.dp)) {
                                    if (correct || wrong) Icon(if (correct) Icons.Default.CheckCircle else Icons.Default.Close,
                                        if (correct) "Correct" else "Incorrect", tint = accent)
                                }
                            }
                        }
                    }
                    if (options.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
