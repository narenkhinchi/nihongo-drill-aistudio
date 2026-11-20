package com.nihongo.vocabdrill.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun EncouragementScreen(remaining: Int, onContinue: () -> Unit) {
    val fall = remember { Animatable(0f) }
    LaunchedEffect(Unit) { fall.animateTo(1f, tween(3200, easing = LinearEasing)) }
    Dialog(onDismissRequest = onContinue, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(Modifier.fillMaxSize().testTag("encouragement_fullscreen"), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                Canvas(Modifier.fillMaxSize()) {
                    val colors = listOf(Color(0xFF8670FF), Color(0xFFFFC857), Color(0xFF37CEB1), Color(0xFFFF83AE))
                    repeat(54) { i ->
                        val x = size.width * ((i * 37 % 101) / 100f)
                        val y = size.height * (fall.value * 1.5f - (i % 9) * 0.07f - 0.2f)
                        rotate(i * 19f + fall.value * 280f, Offset(x, y)) {
                            drawRect(colors[i % colors.size].copy(alpha = 1f - fall.value), Offset(x, y), Size(7.dp.toPx(), 12.dp.toPx()))
                        }
                    }
                }
                Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(Icons.Default.EmojiEvents, null, Modifier.padding(30.dp).size(76.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(28.dp))
                        Text("You’re on a roll!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text("Great work. Keep the momentum!", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        Text("$remaining words to go", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = onContinue, modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().heightIn(min = 56.dp).testTag("continue_encouragement")) {
                        Text("Keep Going", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
