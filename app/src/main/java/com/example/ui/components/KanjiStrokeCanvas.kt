package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentStrokeRed
import com.example.ui.theme.PrimaryText

/**
 * Draws Kanji strokes up to a given step on a Canvas.
 * - Previous strokes: dark PrimaryText (#171717)
 * - Current active stroke: activeColor
 */
@Composable
fun KanjiStrokeRenderer(
  character: String,
  upToStep: Int,
  modifier: Modifier = Modifier,
  showGridLines: Boolean = true,
  activeColor: Color = AccentStrokeRed,
  strokeWidthFactor: Float = 4.8f
) {
  val strokes = remember(character) { StrokeDataProvider.getStrokesForKanji(character) }
  val baseStrokeColor = PrimaryText
  val gridColor = com.example.ui.theme.BorderOutline

  Canvas(modifier = modifier) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    if (showGridLines) {
      val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

      // Vertical center dashed line
      drawLine(
        color = gridColor,
        start = Offset(canvasWidth / 2f, 0f),
        end = Offset(canvasWidth / 2f, canvasHeight),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect
      )

      // Horizontal center dashed line
      drawLine(
        color = gridColor,
        start = Offset(0f, canvasHeight / 2f),
        end = Offset(canvasWidth, canvasHeight / 2f),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect
      )
    }

    if (strokes.isEmpty()) {
      return@Canvas
    }

    val maxStep = upToStep.coerceIn(0, strokes.size - 1)

    strokes.forEachIndexed { index, strokeSegment ->
      if (index > maxStep) return@forEachIndexed

      val isCurrentStroke = (index == maxStep && maxStep < strokes.size - 1)
      val color = if (isCurrentStroke) activeColor else baseStrokeColor
      val actualStrokeWidth = (canvasWidth / 100f) * strokeWidthFactor

      if (strokeSegment.points.size >= 2) {
        val path = Path().apply {
          val first = strokeSegment.points.first()
          moveTo((first.x / 100f) * canvasWidth, (first.y / 100f) * canvasHeight)

          for (i in 1 until strokeSegment.points.size) {
            val pt = strokeSegment.points[i]
            lineTo((pt.x / 100f) * canvasWidth, (pt.y / 100f) * canvasHeight)
          }
        }

        drawPath(
          path = path,
          color = color,
          style = Stroke(
            width = actualStrokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
          )
        )
      }
    }
  }
}
