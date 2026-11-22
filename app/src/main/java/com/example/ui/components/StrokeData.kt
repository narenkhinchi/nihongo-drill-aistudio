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

  @Volatile
  private var remoteStrokes: Map<String, List<StrokeSegment>> = emptyMap()

  @Volatile
  private var remoteGuidance: Map<String, KanjiGuidance> = emptyMap()

  fun applyRemoteStrokeData(
    strokesByCharacter: Map<String, List<StrokeSegment>>,
    guidanceByCharacter: Map<String, KanjiGuidance>
  ) {
    if (strokesByCharacter.isNotEmpty()) {
      remoteStrokes = remoteStrokes + strokesByCharacter
    }
    if (guidanceByCharacter.isNotEmpty()) {
      remoteGuidance = remoteGuidance + guidanceByCharacter
    }
  }

  // Normalized coordinates (0f..100f)
  fun getStrokesForKanji(character: String): List<StrokeSegment> {
    remoteStrokes[character]?.takeIf { it.isNotEmpty() }?.let { return it }
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

      // Lesson 2 Kanji (#11 - #20) — Exact Basic Kanji Book stroke order
      "火" -> listOf(
        StrokeSegment(
          points = listOf(Offset(25f, 36f), Offset(28f, 48f)),
          description = "Left short dot",
          directionArrow = "↘",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(Offset(76f, 32f), Offset(64f, 47f)),
          description = "Right short inward stroke",
          directionArrow = "↙",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(Offset(50f, 16f), Offset(48f, 48f), Offset(36f, 70f), Offset(18f, 84f)),
          description = "Center sweep down-left",
          directionArrow = "↙",
          stepNumber = 3
        ),
        StrokeSegment(
          points = listOf(Offset(48f, 48f), Offset(64f, 68f), Offset(84f, 84f)),
          description = "Right leg down-right",
          directionArrow = "↘",
          stepNumber = 4
        )
      )

      "水" -> listOf(
        StrokeSegment(
          points = listOf(Offset(50f, 14f), Offset(50f, 84f), Offset(41f, 76f)),
          description = "Center vertical & hook",
          directionArrow = "↓",
          stepNumber = 1
        ),
        StrokeSegment(
          points = listOf(Offset(20f, 42f), Offset(38f, 42f), Offset(30f, 62f), Offset(16f, 78f)),
          description = "Left across then sweep",
          directionArrow = "↴",
          stepNumber = 2
        ),
        StrokeSegment(
          points = listOf(Offset(78f, 30f), Offset(56f, 46f)),
          description = "Upper-right inward flick",
          directionArrow = "↙",
          stepNumber = 3
        ),
        StrokeSegment(
          points = listOf(Offset(52f, 46f), Offset(68f, 66f), Offset(84f, 80f)),
          description = "Lower-right sweep down",
          directionArrow = "↘",
          stepNumber = 4
        )
      )

      "金" -> listOf(
        StrokeSegment(points = listOf(Offset(50f, 14f), Offset(34f, 32f), Offset(16f, 46f)), description = "Top-left roof sweep", directionArrow = "↙", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(50f, 16f), Offset(68f, 32f), Offset(86f, 44f)), description = "Top-right roof leg", directionArrow = "↘", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(32f, 43f), Offset(68f, 43f)), description = "Upper horizontal bar", directionArrow = "→", stepNumber = 3),
        StrokeSegment(points = listOf(Offset(26f, 58f), Offset(74f, 58f)), description = "Middle horizontal bar", directionArrow = "→", stepNumber = 4),
        StrokeSegment(points = listOf(Offset(50f, 43f), Offset(50f, 82f)), description = "Center vertical stem", directionArrow = "↓", stepNumber = 5),
        StrokeSegment(points = listOf(Offset(31f, 66f), Offset(37f, 76f)), description = "Left inner dot", directionArrow = "↘", stepNumber = 6),
        StrokeSegment(points = listOf(Offset(69f, 65f), Offset(61f, 76f)), description = "Right inner flick", directionArrow = "↙", stepNumber = 7),
        StrokeSegment(points = listOf(Offset(18f, 82f), Offset(82f, 82f)), description = "Wide bottom base", directionArrow = "→", stepNumber = 8)
      )

      "土" -> listOf(
        StrokeSegment(points = listOf(Offset(26f, 45f), Offset(74f, 45f)), description = "Upper shorter bar", directionArrow = "→", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(50f, 16f), Offset(50f, 82f)), description = "Center vertical down", directionArrow = "↓", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(16f, 82f), Offset(84f, 82f)), description = "Long bottom base", directionArrow = "→", stepNumber = 3)
      )

      "子" -> listOf(
        StrokeSegment(points = listOf(Offset(28f, 20f), Offset(72f, 20f), Offset(52f, 38f)), description = "Top across & inward", directionArrow = "↴", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(50f, 38f), Offset(53f, 58f), Offset(51f, 84f), Offset(41f, 76f)), description = "Curved vertical & hook", directionArrow = "↓", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(16f, 52f), Offset(84f, 52f)), description = "Wide crossbar across", directionArrow = "→", stepNumber = 3)
      )

      "女" -> listOf(
        StrokeSegment(points = listOf(Offset(46f, 16f), Offset(32f, 52f), Offset(52f, 64f), Offset(74f, 78f)), description = "Down-left then down-right", directionArrow = "↙", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(66f, 34f), Offset(58f, 58f), Offset(42f, 74f), Offset(22f, 84f)), description = "Right curved sweep", directionArrow = "↙", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(16f, 42f), Offset(84f, 42f)), description = "Long horizontal bar", directionArrow = "→", stepNumber = 3)
      )

      "学" -> listOf(
        StrokeSegment(points = listOf(Offset(28f, 16f), Offset(33f, 26f)), description = "Top-left dot", directionArrow = "↘", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(48f, 14f), Offset(52f, 24f)), description = "Top-center dot", directionArrow = "↘", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(72f, 16f), Offset(64f, 28f)), description = "Top-right flick", directionArrow = "↙", stepNumber = 3),
        StrokeSegment(points = listOf(Offset(20f, 32f), Offset(18f, 44f)), description = "Left crown drop", directionArrow = "↓", stepNumber = 4),
        StrokeSegment(points = listOf(Offset(20f, 32f), Offset(80f, 32f), Offset(74f, 42f)), description = "Crown across & turn", directionArrow = "↴", stepNumber = 5),
        StrokeSegment(points = listOf(Offset(34f, 45f), Offset(66f, 45f), Offset(51f, 57f)), description = "Inner 子 top turn", directionArrow = "↴", stepNumber = 6),
        StrokeSegment(points = listOf(Offset(50f, 57f), Offset(52f, 72f), Offset(50f, 86f), Offset(41f, 79f)), description = "Inner curved stem & hook", directionArrow = "↓", stepNumber = 7),
        StrokeSegment(points = listOf(Offset(18f, 67f), Offset(82f, 67f)), description = "Bottom crossbar", directionArrow = "→", stepNumber = 8)
      )

      "生" -> listOf(
        StrokeSegment(points = listOf(Offset(32f, 22f), Offset(25f, 38f), Offset(20f, 48f)), description = "Top-left short sweep", directionArrow = "↙", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(28f, 36f), Offset(76f, 36f)), description = "Top horizontal bar", directionArrow = "→", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(50f, 16f), Offset(50f, 82f)), description = "Center vertical down", directionArrow = "↓", stepNumber = 3),
        StrokeSegment(points = listOf(Offset(28f, 58f), Offset(74f, 58f)), description = "Middle horizontal bar", directionArrow = "→", stepNumber = 4),
        StrokeSegment(points = listOf(Offset(16f, 82f), Offset(84f, 82f)), description = "Long bottom base", directionArrow = "→", stepNumber = 5)
      )

      "先" -> listOf(
        StrokeSegment(points = listOf(Offset(34f, 20f), Offset(27f, 34f), Offset(22f, 43f)), description = "Top-left short sweep", directionArrow = "↙", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(31f, 33f), Offset(74f, 33f)), description = "Upper horizontal bar", directionArrow = "→", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(50f, 14f), Offset(50f, 52f)), description = "Center vertical down", directionArrow = "↓", stepNumber = 3),
        StrokeSegment(points = listOf(Offset(18f, 52f), Offset(82f, 52f)), description = "Long middle bar", directionArrow = "→", stepNumber = 4),
        StrokeSegment(points = listOf(Offset(42f, 52f), Offset(38f, 70f), Offset(20f, 84f)), description = "Left curved leg", directionArrow = "↙", stepNumber = 5),
        StrokeSegment(points = listOf(Offset(58f, 52f), Offset(58f, 76f), Offset(64f, 82f), Offset(82f, 82f), Offset(84f, 73f)), description = "Right leg & upward hook", directionArrow = "↳", stepNumber = 6)
      )

      "私" -> listOf(
        StrokeSegment(points = listOf(Offset(44f, 18f), Offset(31f, 24f), Offset(18f, 28f)), description = "Top-left angled sweep", directionArrow = "↙", stepNumber = 1),
        StrokeSegment(points = listOf(Offset(15f, 46f), Offset(47f, 46f)), description = "Left horizontal bar", directionArrow = "→", stepNumber = 2),
        StrokeSegment(points = listOf(Offset(33f, 24f), Offset(33f, 86f)), description = "Left vertical stem", directionArrow = "↓", stepNumber = 3),
        StrokeSegment(points = listOf(Offset(33f, 46f), Offset(24f, 64f), Offset(14f, 76f)), description = "Left sweep down-left", directionArrow = "↙", stepNumber = 4),
        StrokeSegment(points = listOf(Offset(35f, 54f), Offset(45f, 64f)), description = "Left small dot", directionArrow = "↘", stepNumber = 5),
        StrokeSegment(points = listOf(Offset(70f, 28f), Offset(60f, 58f), Offset(52f, 78f), Offset(84f, 72f)), description = "Right ム angled stroke", directionArrow = "↳", stepNumber = 6),
        StrokeSegment(points = listOf(Offset(76f, 58f), Offset(86f, 80f)), description = "Right closing dot", directionArrow = "↘", stepNumber = 7)
      )

      else -> emptyList()
    }
  }

  fun getGuidance(character: String): KanjiGuidance {
    remoteGuidance[character]?.let { return it }
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
      "火" -> KanjiGuidance(
        rule = "Side sparks first (left then right), then central 人 flame",
        tips = listOf(
          "1. Left small dot",
          "2. Right inward flick",
          "3. Tall center sweep down-left",
          "4. Right diagonal leg down"
        )
      )
      "水" -> KanjiGuidance(
        rule = "Center hook first, then left side, then right side strokes",
        tips = listOf(
          "1. Center vertical line with hook",
          "2. Left angled sweep",
          "3. Upper-right short flick",
          "4. Lower-right sweep down"
        )
      )
      "金" -> KanjiGuidance(
        rule = "Roof first, inner bars & center stem, two dots, close base last",
        tips = listOf(
          "1-2. Wide roof strokes",
          "3-5. Two bars & center vertical",
          "6-7. Left dot & right flick",
          "8. Wide bottom base"
        )
      )
      "土" -> KanjiGuidance(
        rule = "Short upper bar, vertical stem, then longer bottom base",
        tips = listOf(
          "1. Shorter upper horizontal",
          "2. Center vertical down",
          "3. Longer bottom base"
        )
      )
      "子" -> KanjiGuidance(
        rule = "Top angle, curved body with hook, wide horizontal arms last",
        tips = listOf(
          "1. Top across and inward",
          "2. Curved vertical with hook",
          "3. Long middle crossbar"
        )
      )
      "女" -> KanjiGuidance(
        rule = "Bent left-right stroke first, sweeping diagonal, crossbar last",
        tips = listOf(
          "1. Curve down-left then angle right",
          "2. Curved diagonal sweep left",
          "3. Long horizontal across top"
        )
      )
      "学" -> KanjiGuidance(
        rule = "Three top dots, crown frame, then 子 inside at bottom",
        tips = listOf(
          "1-3. Three top dots left to right",
          "4-5. Crown top border",
          "6-8. Inner 子 with final crossbar"
        )
      )
      "生" -> KanjiGuidance(
        rule = "Left sprout sweep, top bar, center stem, middle bar, wide base",
        tips = listOf(
          "1. Upper-left short sweep",
          "2. Top horizontal",
          "3. Center vertical stem",
          "4-5. Middle bar & long base"
        )
      )
      "先" -> KanjiGuidance(
        rule = "Upper half (sweep, bars, vertical) first, then two legs below",
        tips = listOf(
          "1-4. Upper sprout & wide middle bar",
          "5. Left curved leg",
          "6. Right leg curving right & hooking up"
        )
      )
      "私" -> KanjiGuidance(
        rule = "Complete left grain radical (禾) first, then right ム",
        tips = listOf(
          "1-5. Left radical (禾)",
          "6. Right angled stroke",
          "7. Right closing dot"
        )
      )
      else -> KanjiGuidance(
        rule = "Top to bottom, left to right",
        tips = emptyList()
      )
    }
  }
}
