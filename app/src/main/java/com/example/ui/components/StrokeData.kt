package com.example.ui.components

import androidx.compose.ui.geometry.Offset

data class StrokeSegment(
  val points: List<Offset>,
  val description: String = "",
  val directionArrow: String = "↓",
  val stepNumber: Int = 1
)

data class KanjiGuidance(
  val rule: String,
  val tips: List<String>
)

object StrokeDataProvider {

  // Normalized coordinates (0f..100f)
  fun getStrokesForKanji(character: String): List<StrokeSegment> {
    return when (character) {
      "日" -> listOf(
        StrokeSegment(
          points = listOf(Offset(28f, 20f), Offset(28f, 80f)),
          description = "Top to bottom",
          directionArrow = "↓",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(Offset(28f, 20f), Offset(72f, 20f), Offset(72f, 80f)),
          description = "Across then down",
          directionArrow = "↴",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(Offset(28f, 49f), Offset(72f, 49f)),
          description = "Left to right",
          directionArrow = "→",
          stepNumber = 3
        ),
        StrokeSegment(
          points = listOf(Offset(28f, 78f), Offset(72f, 78f)),
          description = "Close bottom",
          directionArrow = "→",
          stepNumber = 4
        )
      )

      "月" -> listOf(
        StrokeSegment(
          points = listOf(
            Offset(32f, 18f),
            Offset(32f, 50f),
            Offset(28f, 70f),
            Offset(20f, 84f)
          ),
          description = "Down and sweep left",
          directionArrow = "↙",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(
            Offset(32f, 18f),
            Offset(72f, 18f),
            Offset(72f, 82f),
            Offset(63f, 76f)
          ),
          description = "Across, down & hook",
          directionArrow = "↴",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(Offset(32f, 40f), Offset(72f, 40f)),
          description = "Upper inner bar",
          directionArrow = "→",
          stepNumber = 3
        ),
        StrokeSegment(
          points = listOf(Offset(30f, 60f), Offset(72f, 60f)),
          description = "Lower inner bar",
          directionArrow = "→",
          stepNumber = 4
        )
      )

      "木" -> listOf(
        StrokeSegment(
          points = listOf(Offset(18f, 38f), Offset(82f, 38f)),
          description = "Horizontal branch",
          directionArrow = "→",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(Offset(50f, 14f), Offset(50f, 86f)),
          description = "Vertical trunk",
          directionArrow = "↓",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(
            Offset(50f, 38f),
            Offset(36f, 62f),
            Offset(18f, 80f)
          ),
          description = "Left sweep down",
          directionArrow = "↙",
          stepNumber = 3
        ),
        StrokeSegment(
          points = listOf(
            Offset(50f, 38f),
            Offset(64f, 62f),
            Offset(82f, 80f)
          ),
          description = "Right sweep down",
          directionArrow = "↘",
          stepNumber = 4
        )
      )

      "山" -> listOf(
        StrokeSegment(
          points = listOf(Offset(50f, 18f), Offset(50f, 76f)),
          description = "Center peak down",
          directionArrow = "↓",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(Offset(24f, 42f), Offset(24f, 76f), Offset(76f, 76f)),
          description = "Left down then across",
          directionArrow = "↳",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(Offset(76f, 40f), Offset(76f, 80f)),
          description = "Right peak down",
          directionArrow = "↓",
          stepNumber = 3
        )
      )

      "川" -> listOf(
        StrokeSegment(
          points = listOf(
            Offset(28f, 22f),
            Offset(28f, 56f),
            Offset(24f, 72f),
            Offset(18f, 82f)
          ),
          description = "Left curved stream",
          directionArrow = "↙",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(Offset(50f, 26f), Offset(50f, 70f)),
          description = "Middle stream down",
          directionArrow = "↓",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(Offset(74f, 18f), Offset(74f, 84f)),
          description = "Right long stream",
          directionArrow = "↓",
          stepNumber = 3
        )
      )

      "田" -> listOf(
        StrokeSegment(
          points = listOf(Offset(22f, 24f), Offset(22f, 78f)),
          description = "Left border down",
          directionArrow = "↓",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(Offset(22f, 24f), Offset(78f, 24f), Offset(78f, 78f)),
          description = "Top & right border",
          directionArrow = "↴",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(Offset(50f, 24f), Offset(50f, 76f)),
          description = "Center vertical",
          directionArrow = "↓",
          stepNumber = 3
        ),
        StrokeSegment(
          points = listOf(Offset(22f, 50f), Offset(78f, 50f)),
          description = "Center horizontal",
          directionArrow = "→",
          stepNumber = 4
        ),
        StrokeSegment(
          points = listOf(Offset(22f, 76f), Offset(78f, 76f)),
          description = "Close bottom",
          directionArrow = "→",
          stepNumber = 5
        )
      )

      "人" -> listOf(
        StrokeSegment(
          points = listOf(
            Offset(50f, 18f),
            Offset(42f, 46f),
            Offset(30f, 68f),
            Offset(16f, 84f)
          ),
          description = "Left sweep down",
          directionArrow = "↙",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(
            Offset(42f, 46f),
            Offset(58f, 68f),
            Offset(84f, 84f)
          ),
          description = "Right leg down",
          directionArrow = "↘",
          stepNumber = 2
        )
      )

      "口" -> listOf(
        StrokeSegment(
          points = listOf(Offset(25f, 28f), Offset(27f, 76f)),
          description = "Left side down",
          directionArrow = "↓",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(Offset(25f, 28f), Offset(75f, 28f), Offset(73f, 74f)),
          description = "Top across & right down",
          directionArrow = "↴",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(Offset(27f, 74f), Offset(75f, 74f)),
          description = "Close bottom",
          directionArrow = "→",
          stepNumber = 3
        )
      )

      "車" -> listOf(
        StrokeSegment(points = listOf(Offset(24f, 22f), Offset(76f, 22f)), description = "Top bar", directionArrow = "→", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(26f, 35f), Offset(26f, 61f)), description = "Left box side", directionArrow = "↓", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(26f, 35f), Offset(74f, 35f), Offset(74f, 61f)), description = "Top & right box", directionArrow = "↴", stepNumber = 3),
        StrokeSegment(points = listOf(Offset(26f, 48f), Offset(74f, 48f)), description = "Middle inner bar", directionArrow = "→", stepNumber = 4),
        StrokeSegment(points = listOf(Offset(26f, 61f), Offset(74f, 61f)), description = "Close box bottom", directionArrow = "→", stepNumber = 5),
        StrokeSegment(points = listOf(Offset(16f, 73f), Offset(84f, 73f)), description = "Wide axle bar", directionArrow = "→", stepNumber = 6),
        StrokeSegment(points = listOf(Offset(50f, 11f), Offset(50f, 89f)), description = "Center vertical line", directionArrow = "↓", stepNumber = 7)
      )

      "門" -> listOf(
        StrokeSegment(points = listOf(Offset(20f, 20f), Offset(20f, 84f)), description = "Left pillar down", directionArrow = "↓", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(20f, 20f), Offset(43f, 20f), Offset(43f, 46f)), description = "Left top & inner", directionArrow = "↴", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(20f, 33f), Offset(43f, 33f)), description = "Left mid bar", directionArrow = "→", stepNumber = 3),
        StrokeSegment(points = listOf(Offset(20f, 46f), Offset(43f, 46f)), description = "Left low bar", directionArrow = "→", stepNumber = 4),
        StrokeSegment(points = listOf(Offset(57f, 20f), Offset(57f, 46f)), description = "Right inner down", directionArrow = "↓", stepNumber = 5),
        StrokeSegment(points = listOf(Offset(57f, 20f), Offset(80f, 20f), Offset(80f, 82f), Offset(72f, 76f)), description = "Right pillar & hook", directionArrow = "↴", stepNumber = 6),
        StrokeSegment(points = listOf(Offset(57f, 33f), Offset(80f, 33f)), description = "Right mid bar", directionArrow = "→", stepNumber = 7),
        StrokeSegment(points = listOf(Offset(57f, 46f), Offset(80f, 46f)), description = "Right low bar", directionArrow = "→", stepNumber = 8)
      )

      else -> emptyList()
    }
  }

  fun getGuidance(character: String): KanjiGuidance {
    return when (character) {
      "日" -> KanjiGuidance(
        rule = "Enclosing Rule: Fill inside before closing the bottom",
        tips = listOf(
          "1. Left vertical down",
          "2. Top and right boundary",
          "3. Middle bar connects sides",
          "4. Bottom bar closes the box"
        )
      )
      "月" -> KanjiGuidance(
        rule = "Left to right, top to bottom with hook",
        tips = listOf(
          "1. Left sweep",
          "2. Top right with hook",
          "3. First inner bar",
          "4. Second inner bar"
        )
      )
      "木" -> KanjiGuidance(
        rule = "Horizontal before vertical, then left to right",
        tips = listOf(
          "1. Horizontal branch across",
          "2. Center vertical trunk down",
          "3. Left diagonal root",
          "4. Right diagonal root"
        )
      )
      "山" -> KanjiGuidance(
        rule = "Center first, then symmetrical sides",
        tips = listOf(
          "1. Center peak first",
          "2. Left down and across",
          "3. Right peak down"
        )
      )
      "川" -> KanjiGuidance(
        rule = "Left to right flowing river streams",
        tips = listOf(
          "1. Left curved flow",
          "2. Middle short flow",
          "3. Right long straight flow"
        )
      )
      "田" -> KanjiGuidance(
        rule = "Outer frame first, cross in center, close bottom last",
        tips = listOf(
          "1. Left border",
          "2. Top and right",
          "3. Center vertical",
          "4. Center horizontal",
          "5. Bottom closing"
        )
      )
      "人" -> KanjiGuidance(
        rule = "Left sweep first, then right supporting leg",
        tips = listOf(
          "1. Left stroke down",
          "2. Right diagonal leg"
        )
      )
      "口" -> KanjiGuidance(
        rule = "Left down, top-right corner, close bottom",
        tips = listOf(
          "1. Left vertical",
          "2. Top and right",
          "3. Bottom closing"
        )
      )
      "車" -> KanjiGuidance(
        rule = "Top roof, cabin frame, middle bars, wide axle, then center shaft",
        tips = listOf(
          "1. Top roof",
          "2-5. Cabin structure",
          "6. Wide axle",
          "7. Center axle through all"
        )
      )
      "門" -> KanjiGuidance(
        rule = "Left gate post & bars first, then right post & bars",
        tips = listOf(
          "1-4. Left gate half",
          "5-8. Right gate half"
        )
      )
      else -> KanjiGuidance(
        rule = "Top to bottom, left to right",
        tips = emptyList()
      )
    }
  }
}
