package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Kanji
import com.example.ui.components.KanjiStrokeRenderer
import com.example.ui.components.StrokeDataProvider
import com.example.ui.theme.AccentStrokeRed
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.BrandPurpleLight
import com.example.ui.theme.BrandPurpleVeryLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.MainBackground
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftPurpleBorder
import com.example.ui.theme.isKanjiDarkTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeWritingScreen(
  kanji: Kanji,
  lessonTitle: String = "Lesson 1",
  currentIndex: Int,
  totalKanji: Int,
  isBookmarked: Boolean,
  onToggleBookmark: () -> Unit,
  onBack: () -> Unit,
  onPlayAudio: () -> Unit,
  onNextKanji: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val strokeSegments = remember(kanji.character) {
    StrokeDataProvider.getStrokesForKanji(kanji.character)
  }
  val kanjiGuidance = remember(kanji.character) {
    StrokeDataProvider.getGuidance(kanji.character)
  }

  // User drawn strokes for practice mode
  val drawnStrokes = remember(kanji.number) { mutableStateListOf<List<Offset>>() }
  var inProgressStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

  // Show faint watermark guide toggle in Practice mode
  var showGuideWatermark by remember { mutableStateOf(true) }

  // Completion state
  var isEvaluated by remember(kanji.number) { mutableStateOf(false) }
  val strokeCount = drawnStrokes.size
  val isStrokeMatch = strokeCount == kanji.strokeCount

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MainBackground),
    containerColor = MainBackground,
    bottomBar = {
      // Bottom sticky actions
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding(),
        color = CardSurface,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, BorderOutline)
      ) {
        if (!isEvaluated) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 14.dp)
          ) {
            Button(
              onClick = {
                if (drawnStrokes.isNotEmpty()) {
                  isEvaluated = true
                }
              },
              enabled = drawnStrokes.isNotEmpty(),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("check_kanji_button"),
              shape = RoundedCornerShape(24.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandPurple,
                contentColor = Color.White
              )
            ) {
              Text(
                text = "Check Kanji  ✓",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }
        } else {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = {
                drawnStrokes.clear()
                isEvaluated = false
              },
              modifier = Modifier
                .weight(1f)
                .height(48.dp),
              shape = RoundedCornerShape(24.dp),
              border = BorderStroke(1.2.dp, BrandPurple),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = CardSurface,
                contentColor = BrandPurple
              )
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Practice Again", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
              onClick = {
                drawnStrokes.clear()
                isEvaluated = false
                onNextKanji()
              },
              modifier = Modifier
                .weight(1f)
                .height(48.dp),
              shape = RoundedCornerShape(24.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BrandPurple,
                contentColor = Color.White
              )
            ) {
              Text("Next Kanji", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Spacer(modifier = Modifier.width(6.dp))
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(17.dp))
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .statusBarsPadding()
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "$lessonTitle · Practice Writing",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
          )
          Text(
            text = "Kanji ${currentIndex + 1} of $totalKanji",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandPurple
          )
        }

        IconButton(onClick = onToggleBookmark) {
          Icon(
            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = "Bookmark",
            tint = if (isBookmarked) BrandPurple else SecondaryText
          )
        }
      }

      // Progress Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val progress = (currentIndex + 1).toFloat() / totalKanji.toFloat()
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .weight(1f)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = BrandPurple,
          trackColor = BorderOutline,
          strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
          text = "${currentIndex + 1} / $totalKanji",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = SecondaryText
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 2. Compact Prompt Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.2.dp, BorderOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(BrandPurpleVeryLight)
              .border(1.dp, SoftPurpleBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = kanji.character,
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryText
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = kanji.meaning,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = PrimaryText
            )
            Text(
              text = "${kanji.strokeCount} strokes · ${kanjiGuidance.rule}",
              fontSize = 11.5.sp,
              color = SecondaryText,
              maxLines = 1
            )
          }

          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(BrandPurpleVeryLight)
              .border(1.dp, SoftPurpleBorder, CircleShape)
              .clickable(onClick = onPlayAudio),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = "Pronounce",
              tint = BrandPurple,
              modifier = Modifier.size(19.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Visual Step-by-Step Stroke Sequence Strip
      val infiniteTransition = rememberInfiniteTransition(label = "stroke_tracer")
      val traceProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 1350, easing = LinearEasing),
          repeatMode = RepeatMode.Restart
        ),
        label = "trace_progress"
      )

      val isAllStrokesDrawn = strokeCount >= kanji.strokeCount

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        strokeSegments.forEachIndexed { index, segment ->
          val isCompleted = index < strokeCount
          val isCurrent = index == strokeCount && !isAllStrokesDrawn

          val boxBorderColor = when {
            isCurrent -> BrandPurple
            isCompleted -> Color(0xFF10B981)
            else -> BorderOutline
          }
          val boxBgColor = when {
            isCurrent -> BrandPurpleVeryLight
            isCompleted -> if (isKanjiDarkTheme) Color(0xFF064E3B) else Color(0xFFF0FDF4)
            else -> CardSurface
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(boxBgColor)
                .border(if (isCurrent) 2.dp else 1.2.dp, boxBorderColor, RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              KanjiStrokeRenderer(
                character = kanji.character,
                upToStep = index,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(6.dp),
                showGridLines = false,
                activeColor = if (isCurrent) BrandPurple else AccentStrokeRed,
                strokeWidthFactor = 5.4f
              )

              // Top-right badge: checkmark if completed, direction arrow if current
              if (isCompleted) {
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                  )
                }
              } else if (isCurrent) {
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(BrandPurple),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = segment.directionArrow,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
              text = "${index + 1}",
              fontSize = 11.5.sp,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
              color = when {
                isCurrent -> BrandPurple
                isCompleted -> Color(0xFF059669)
                else -> SecondaryText
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 4. Main Canvas: Interactive Drawing with On-Canvas Animated Stroke Guide
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f),
        contentAlignment = Alignment.Center
      ) {
        val darkMode = isKanjiDarkTheme
        val borderColor = if (isEvaluated) Color(0xFF10B981) else BorderOutline
        val borderWidth = if (isEvaluated) 2.5.dp else 1.5.dp
        val canvasGridColor = BorderOutline
        val watermarkStrokeColor = if (darkMode) Color(0xFF263859) else Color(0xFFE5E5F0)
        val guideBrandColor = BrandPurple
        val canvasInkColor = PrimaryText

        Card(
          modifier = Modifier.fillMaxSize(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = CardSurface),
          border = BorderStroke(borderWidth, borderColor),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            // Traditional Japanese Genkouyoushi crosshairs
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

              drawLine(
                color = canvasGridColor,
                start = Offset(w / 2f, 0f),
                end = Offset(w / 2f, h),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = dashEffect
              )
              drawLine(
                color = canvasGridColor,
                start = Offset(0f, h / 2f),
                end = Offset(w, h / 2f),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = dashEffect
              )
            }

            // Vector Watermark + Active Animated Stroke Trace Guide
            if (showGuideWatermark) {
              Canvas(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(20.dp)
              ) {
                val cw = size.width
                val ch = size.height
                val guideStrokeWidth = 13.dp.toPx()

                // 1. Draw all strokes in faint watermark grey so full Kanji shape is visible
                strokeSegments.forEach { seg ->
                  if (seg.points.size >= 2) {
                    val p = Path().apply {
                      val first = seg.points.first()
                      moveTo((first.x / 100f) * cw, (first.y / 100f) * ch)
                      for (i in 1 until seg.points.size) {
                        val pt = seg.points[i]
                        lineTo((pt.x / 100f) * cw, (pt.y / 100f) * ch)
                      }
                    }
                    drawPath(
                      path = p,
                      color = watermarkStrokeColor,
                      style = Stroke(
                        width = guideStrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                      )
                    )
                  }
                }

                // 2. Highlight ONLY the current active stroke to draw next with animated direction tracer
                if (strokeCount < strokeSegments.size && !isEvaluated) {
                  val targetSeg = strokeSegments[strokeCount]
                  if (targetSeg.points.size >= 2) {
                    val activePath = Path().apply {
                      val first = targetSeg.points.first()
                      moveTo((first.x / 100f) * cw, (first.y / 100f) * ch)
                      for (i in 1 until targetSeg.points.size) {
                        val pt = targetSeg.points[i]
                        lineTo((pt.x / 100f) * cw, (pt.y / 100f) * ch)
                      }
                    }

                    // Soft purple highlight along the target stroke
                    drawPath(
                      path = activePath,
                      color = guideBrandColor.copy(alpha = 0.25f),
                      style = Stroke(
                        width = guideStrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                      )
                    )

                    // Center dashed directional guideline inside the active stroke
                    drawPath(
                      path = activePath,
                      color = guideBrandColor.copy(alpha = 0.65f),
                      style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                      )
                    )

                    // Directional arrowhead at the end of the active stroke
                    val pLast = targetSeg.points.last()
                    val pPrev = targetSeg.points[targetSeg.points.size - 2]
                    val endX = (pLast.x / 100f) * cw
                    val endY = (pLast.y / 100f) * ch
                    val prevX = (pPrev.x / 100f) * cw
                    val prevY = (pPrev.y / 100f) * ch
                    val angle = kotlin.math.atan2((endY - prevY).toDouble(), (endX - prevX).toDouble())
                    val arrowLen = 10.dp.toPx()
                    val arrowAngle = Math.toRadians(28.0)

                    val arrowPath = Path().apply {
                      moveTo(endX, endY)
                      lineTo(
                        (endX - arrowLen * kotlin.math.cos(angle - arrowAngle)).toFloat(),
                        (endY - arrowLen * kotlin.math.sin(angle - arrowAngle)).toFloat()
                      )
                      moveTo(endX, endY)
                      lineTo(
                        (endX - arrowLen * kotlin.math.cos(angle + arrowAngle)).toFloat(),
                        (endY - arrowLen * kotlin.math.sin(angle + arrowAngle)).toFloat()
                      )
                    }
                    drawPath(
                      path = arrowPath,
                      color = guideBrandColor,
                      style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Animated tracer dot gliding from start to end of the active stroke
                    val tracerPos = interpolatePointAlongStroke(targetSeg.points, traceProgress, cw, ch)
                    drawCircle(
                      color = guideBrandColor,
                      radius = 6.5.dp.toPx(),
                      center = tracerPos
                    )
                    drawCircle(
                      color = Color.White,
                      radius = 2.8.dp.toPx(),
                      center = tracerPos
                    )
                  }
                }
              }
            }

            // Real-time smooth Calligraphy Touch & Stylus Canvas
            Canvas(
              modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .pointerInput(kanji.number) {
                  detectDragGestures(
                    onDragStart = { offset ->
                      if (!isEvaluated) {
                        inProgressStroke = listOf(offset)
                      }
                    },
                    onDrag = { change, _ ->
                      if (!isEvaluated) {
                        change.consume()
                        inProgressStroke = inProgressStroke + change.position
                      }
                    },
                    onDragEnd = {
                      if (inProgressStroke.isNotEmpty() && !isEvaluated) {
                        drawnStrokes.add(inProgressStroke)
                        inProgressStroke = emptyList()
                      }
                    },
                    onDragCancel = {
                      inProgressStroke = emptyList()
                    }
                  )
                }
            ) {
              val strokeWidthPx = 13.dp.toPx()
              val inkColor = canvasInkColor

              // Committed strokes
              drawnStrokes.forEach { strokePoints ->
                if (strokePoints.size >= 2) {
                  val path = Path().apply {
                    moveTo(strokePoints.first().x, strokePoints.first().y)
                    for (i in 1 until strokePoints.size) {
                      lineTo(strokePoints[i].x, strokePoints[i].y)
                    }
                  }
                  drawPath(
                    path = path,
                    color = inkColor,
                    style = Stroke(
                      width = strokeWidthPx,
                      cap = StrokeCap.Round,
                      join = StrokeJoin.Round
                    )
                  )
                } else if (strokePoints.size == 1) {
                  drawCircle(
                    color = inkColor,
                    radius = strokeWidthPx / 2f,
                    center = strokePoints.first()
                  )
                }
              }

              // In-progress stroke
              if (inProgressStroke.size >= 2) {
                val path = Path().apply {
                  moveTo(inProgressStroke.first().x, inProgressStroke.first().y)
                  for (i in 1 until inProgressStroke.size) {
                    lineTo(inProgressStroke[i].x, inProgressStroke[i].y)
                  }
                }
                drawPath(
                  path = path,
                  color = inkColor,
                  style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                  )
                )
              } else if (inProgressStroke.size == 1) {
                drawCircle(
                  color = inkColor,
                  radius = strokeWidthPx / 2f,
                  center = inProgressStroke.first()
                )
              }
            }
          }
        }

        // Green checkmark badge on complete
        if (isEvaluated) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 12.dp, end = 14.dp)
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xFF10B981)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Practice Controls: Undo, Clear, Guide Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = {
              if (drawnStrokes.isNotEmpty()) {
                drawnStrokes.removeAt(drawnStrokes.size - 1)
                isEvaluated = false
              }
            },
            enabled = drawnStrokes.isNotEmpty() && !isEvaluated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderOutline),
            modifier = Modifier.height(38.dp)
          ) {
            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Undo", fontSize = 13.sp)
          }

          OutlinedButton(
            onClick = {
              drawnStrokes.clear()
              isEvaluated = false
            },
            enabled = drawnStrokes.isNotEmpty(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderOutline),
            modifier = Modifier.height(38.dp)
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear", fontSize = 13.sp)
          }
        }

        // Guide watermark toggle
        FilterChip(
          selected = showGuideWatermark,
          onClick = { showGuideWatermark = !showGuideWatermark },
          label = { Text(if (showGuideWatermark) "Guide ON" else "Guide OFF", fontSize = 12.5.sp) },
          leadingIcon = {
            Icon(
              if (showGuideWatermark) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = null,
              modifier = Modifier.size(15.dp)
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandPurpleLight,
            selectedLabelColor = BrandPurple
          )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 7. Success Evaluation Banner
      AnimatedVisibility(
        visible = isEvaluated,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        val isDark = isKanjiDarkTheme
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = if (isDark) Color(0xFF064E3B) else Color(0xFFECFDF5),
          border = BorderStroke(1.dp, if (isDark) Color(0xFF10B981) else Color(0xFFA7F3D0))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Text(
                text = if (isStrokeMatch) "Well Done! Correct Stroke Order" else "Practice Complete!",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)
              )
              Text(
                text = "You completed all ${kanji.strokeCount} strokes following proper Japanese direction.",
                fontSize = 12.sp,
                color = if (isDark) Color(0xFFD1FAE5) else Color(0xFF047857)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

private fun interpolatePointAlongStroke(
  points: List<Offset>,
  progress: Float,
  canvasWidth: Float,
  canvasHeight: Float
): Offset {
  if (points.isEmpty()) return Offset.Zero
  if (points.size == 1) {
    val pt = points.first()
    return Offset((pt.x / 100f) * canvasWidth, (pt.y / 100f) * canvasHeight)
  }

  val scaled = points.map { Offset((it.x / 100f) * canvasWidth, (it.y / 100f) * canvasHeight) }
  val segmentLengths = FloatArray(scaled.size - 1)
  var totalLength = 0f

  for (i in 0 until scaled.size - 1) {
    val dx = scaled[i + 1].x - scaled[i].x
    val dy = scaled[i + 1].y - scaled[i].y
    val len = kotlin.math.sqrt(dx * dx + dy * dy)
    segmentLengths[i] = len
    totalLength += len
  }

  if (totalLength <= 0.001f) return scaled.first()

  val targetDist = progress.coerceIn(0f, 1f) * totalLength
  var accumulated = 0f

  for (i in segmentLengths.indices) {
    val segLen = segmentLengths[i]
    if (accumulated + segLen >= targetDist || i == segmentLengths.lastIndex) {
      val localT = if (segLen > 0f) ((targetDist - accumulated) / segLen).coerceIn(0f, 1f) else 0f
      val start = scaled[i]
      val end = scaled[i + 1]
      return Offset(
        x = start.x + (end.x - start.x) * localT,
        y = start.y + (end.y - start.y) * localT
      )
    }
    accumulated += segLen
  }

  return scaled.last()
}

