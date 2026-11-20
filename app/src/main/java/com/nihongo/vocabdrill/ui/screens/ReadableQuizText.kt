package com.nihongo.vocabdrill.ui.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp

/** Measure actual glyphs and available width, never guess from character count.
 * Long phrases can wrap without clipping; line height scales with the final font size.
 */
@Composable
internal fun ReadableQuizText(text: String, modifier: Modifier = Modifier, maximumSize: Int = 44, minimumSize: Int = 28) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = with(density) { maxWidth.roundToPx() }.coerceAtLeast(1)
        // Measure with the same complete typography Text renders. Inheriting extra
        // letter spacing only at render time can add a line after height is measured.
        val base = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, letterSpacing = 0.sp)
        fun styleAt(size: Int): TextStyle {
            // Android's large-font scaling is nonlinear: derive line height from
            // the actual rendered font pixels, then convert back to sp.
            val height = with(density) { (size.sp.toPx() * 1.45f).toSp() }
            return base.copy(fontSize = size.sp, lineHeight = height)
        }
        val chosen = (maximumSize downTo minimumSize step 2).firstOrNull { size ->
            val layout = measurer.measure(text, styleAt(size), constraints = Constraints(maxWidth = width))
            layout.lineCount == 1 && !layout.hasVisualOverflow
        } ?: minimumSize
        val finalStyle = styleAt(chosen)
        val layout = measurer.measure(text, finalStyle, constraints = Constraints(maxWidth = width))
        var renderedLines by remember(text, chosen, width, density.fontScale) { mutableIntStateOf(0) }
        // Reserve every wrapped line explicitly. Some CJK paragraphs containing
        // spaces report a smaller intrinsic height than their final line count.
        val safeHeight = with(density) {
            maxOf(layout.size.height.toFloat(), maxOf(layout.lineCount, renderedLines) * finalStyle.lineHeight.toPx() + chosen.sp.toPx() * 0.25f).toDp()
        }
        key(text, chosen, width, density.fontScale) {
            Text(text, modifier.fillMaxWidth().heightIn(min = safeHeight), style = finalStyle,
                color = MaterialTheme.colorScheme.onSurface, softWrap = true,
                onTextLayout = { if (renderedLines != it.lineCount) renderedLines = it.lineCount })
        }
    }
}
