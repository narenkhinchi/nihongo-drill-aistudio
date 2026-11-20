package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Practice writing box preview item with dashed grid lines and faint character
 * used on the Practice Writing row in KanjiLearningScreen.
 */
@Composable
fun PracticeWritingBox(
  character: String,
  size: Dp = 38.dp,
  onClick: () -> Unit = {}
) {
  val boxBg = com.example.ui.theme.CardSurfaceElevated
  val boxBorder = com.example.ui.theme.BorderOutline
  val gridLineColor = com.example.ui.theme.BorderOutline
  val ghostCharColor = com.example.ui.theme.SecondaryText.copy(alpha = 0.55f)

  Box(
    modifier = Modifier
      .size(size)
      .clip(RoundedCornerShape(8.dp))
      .background(boxBg)
      .border(1.2.dp, boxBorder, RoundedCornerShape(8.dp))
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size - 6.dp)) {
      val w = this.size.width
      val h = this.size.height
      val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

      drawLine(
        color = gridLineColor,
        start = Offset(w / 2f, 0f),
        end = Offset(w / 2f, h),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect
      )
      drawLine(
        color = gridLineColor,
        start = Offset(0f, h / 2f),
        end = Offset(w, h / 2f),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect
      )
    }

    Text(
      text = character,
      fontSize = 20.sp,
      fontWeight = FontWeight.Medium,
      color = ghostCharColor
    )
  }
}
