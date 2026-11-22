package com.example.data

data class Kanji(
  val number: Int,
  val character: String,
  val meaning: String,
  val level: String = "N5",
  val kunReadings: List<String>,
  val onReadings: List<String>,
  val onReadingsDisplay: List<String>,
  val strokeCount: Int,
  val strokeDescriptions: List<String> = emptyList()
)

data class Vocabulary(
  val kanjiNumber: Int,
  val word: String,
  val reading: String,
  val meaning: String
)

data class KanjiLesson(
  val id: String,
  val lessonNumber: Int,
  val title: String,
  val subtitle: String,
  val level: String,
  val kanjiList: List<Kanji>
)

data class KanjiReadingExercise(
  val id: Int,
  val level: String,
  val lessonNumber: Int,
  val sentenceWithKanji: String,
  val readingHiragana: String,
  val translation: String
)

data class KanjiStoryToken(
  val surface: String,
  val furigana: String
)

data class KanjiStoryVocab(
  val wordWithReading: String,
  val meaning: String,
  val audioText: String
)

data class KanjiStoryPage(
  val pageNumber: Int,
  val plainParagraphs: List<String>,
  val furiganaLines: List<List<KanjiStoryToken>>,
  val englishLines: List<String>
)

data class KanjiStory(
  val id: String,
  val level: String,
  val lessonNumber: Int,
  val title: String,
  val fullPlainParagraphs: List<String>,
  val pages: List<KanjiStoryPage>,
  val vocabulary: List<KanjiStoryVocab>
)

object KanjiRepository {

  val lesson1Kanji: List<Kanji> = listOf(
    Kanji(
      number = 1,
      character = "日",
      meaning = "Sun · Day",
      level = "N5",
      kunReadings = listOf("ひ", "び", "か"),
      onReadings = listOf("ニチ", "ニ", "（ジツ）"),
      onReadingsDisplay = listOf("にち", "に", "じつ"),
      strokeCount = 4,
      strokeDescriptions = listOf(
        "1. Left vertical line going down",
        "2. Top horizontal line turning down on right",
        "3. Middle horizontal stroke connecting the sides",
        "4. Bottom horizontal closing stroke"
      )
    ),
    Kanji(
      number = 2,
      character = "月",
      meaning = "Moon · Month",
      level = "N5",
      kunReadings = listOf("つき"),
      onReadings = listOf("ゲツ", "（ガツ）"),
      onReadingsDisplay = listOf("げつ", "がつ"),
      strokeCount = 4,
      strokeDescriptions = listOf(
        "1. Left curved downward stroke",
        "2. Horizontal stroke turning down with a hook at bottom",
        "3. Upper inside horizontal stroke",
        "4. Lower inside horizontal stroke"
      )
    ),
    Kanji(
      number = 3,
      character = "木",
      meaning = "Tree · Wood",
      level = "N5",
      kunReadings = listOf("き"),
      onReadings = listOf("モク", "ボク"),
      onReadingsDisplay = listOf("もく", "ぼく"),
      strokeCount = 4,
      strokeDescriptions = listOf(
        "1. Horizontal branch stroke across",
        "2. Vertical trunk stroke going straight down",
        "3. Left diagonal root stroke",
        "4. Right diagonal root stroke"
      )
    ),
    Kanji(
      number = 4,
      character = "山",
      meaning = "Mountain",
      level = "N5",
      kunReadings = listOf("やま"),
      onReadings = listOf("サン"),
      onReadingsDisplay = listOf("さん"),
      strokeCount = 3,
      strokeDescriptions = listOf(
        "1. Central tall peak straight down",
        "2. Left stroke going down then across to the right",
        "3. Right peak going straight down to join the base"
      )
    ),
    Kanji(
      number = 5,
      character = "川",
      meaning = "River · Stream",
      level = "N5",
      kunReadings = listOf("かわ", "がわ"),
      onReadings = listOf("セン"),
      onReadingsDisplay = listOf("せん"),
      strokeCount = 3,
      strokeDescriptions = listOf(
        "1. Left flowing stroke curving slightly left",
        "2. Middle straight river bank stroke",
        "3. Right long straight flow going down"
      )
    ),
    Kanji(
      number = 6,
      character = "田",
      meaning = "Rice field",
      level = "N5",
      kunReadings = listOf("た", "だ"),
      onReadings = listOf("デン"),
      onReadingsDisplay = listOf("でん"),
      strokeCount = 5,
      strokeDescriptions = listOf(
        "1. Left outer boundary going down",
        "2. Top boundary turning right and down",
        "3. Inner vertical dividing ridge",
        "4. Inner horizontal dividing ridge",
        "5. Bottom closing boundary stroke"
      )
    ),
    Kanji(
      number = 7,
      character = "人",
      meaning = "Man · Person",
      level = "N5",
      kunReadings = listOf("ひと"),
      onReadings = listOf("ジン", "ニン"),
      onReadingsDisplay = listOf("じん", "にん"),
      strokeCount = 2,
      strokeDescriptions = listOf(
        "1. Left sweeping downward stroke",
        "2. Right supporting diagonal leg stroke"
      )
    ),
    Kanji(
      number = 8,
      character = "口",
      meaning = "Mouth · Opening",
      level = "N5",
      kunReadings = listOf("くち", "ぐち"),
      onReadings = listOf("コウ"),
      onReadingsDisplay = listOf("こう"),
      strokeCount = 3,
      strokeDescriptions = listOf(
        "1. Left vertical lip stroke down",
        "2. Top lip turning down to right corner",
        "3. Bottom closing lip stroke"
      )
    ),
    Kanji(
      number = 9,
      character = "車",
      meaning = "Car · Vehicle",
      level = "N5",
      kunReadings = listOf("くるま"),
      onReadings = listOf("シャ"),
      onReadingsDisplay = listOf("しゃ"),
      strokeCount = 7,
      strokeDescriptions = listOf(
        "1. Top horizontal roof stroke",
        "2. Left vertical cabin boundary",
        "3. Top and right cabin boundary turning down",
        "4. Middle cabin crossbar",
        "5. Bottom cabin crossbar",
        "6. Bottom long axle bar",
        "7. Central axle rod passing through all down"
      )
    ),
    Kanji(
      number = 10,
      character = "門",
      meaning = "Gate · Entrance",
      level = "N5",
      kunReadings = listOf("かど"),
      onReadings = listOf("モン"),
      onReadingsDisplay = listOf("もん"),
      strokeCount = 8,
      strokeDescriptions = listOf(
        "1. Left post top vertical stroke",
        "2. Left upper corner horizontal and down",
        "3. Left lower inner bar",
        "4. Left outer frame post",
        "5. Right post top vertical stroke",
        "6. Right upper corner turning down with hook",
        "7. Right lower inner bar",
        "8. Right outer frame post"
      )
    )
  )

  val lesson2Kanji: List<Kanji> = listOf(
    Kanji(
      number = 11,
      character = "火",
      meaning = "Fire",
      level = "N5",
      kunReadings = listOf("ひ"),
      onReadings = listOf("カ"),
      onReadingsDisplay = listOf("か"),
      strokeCount = 4,
      strokeDescriptions = listOf(
        "1. Left short downward dot",
        "2. Right short inward diagonal stroke",
        "3. Center long curved stroke sweeping down-left",
        "4. Right diagonal leg sweeping down-right from center"
      )
    ),
    Kanji(
      number = 12,
      character = "水",
      meaning = "Water",
      level = "N5",
      kunReadings = listOf("みず"),
      onReadings = listOf("スイ"),
      onReadingsDisplay = listOf("すい"),
      strokeCount = 4,
      strokeDescriptions = listOf(
        "1. Central vertical stroke going down with a small left hook",
        "2. Left horizontal stroke turning sharply down-left",
        "3. Upper-right short diagonal inward flick",
        "4. Lower-right diagonal stroke sweeping down-right"
      )
    ),
    Kanji(
      number = 13,
      character = "金",
      meaning = "Gold · Money · Metal",
      level = "N5",
      kunReadings = listOf("かね"),
      onReadings = listOf("キン"),
      onReadingsDisplay = listOf("きん"),
      strokeCount = 8,
      strokeDescriptions = listOf(
        "1. Top-left sweeping roof stroke",
        "2. Top-right diagonal roof stroke",
        "3. Upper short horizontal stroke inside roof",
        "4. Middle horizontal crossbar",
        "5. Central vertical stem going straight down",
        "6. Left small dot",
        "7. Right small inward flick",
        "8. Bottom long horizontal closing stroke"
      )
    ),
    Kanji(
      number = 14,
      character = "土",
      meaning = "Ground · Earth · Soil",
      level = "N5",
      kunReadings = listOf("つち"),
      onReadings = listOf("ド"),
      onReadingsDisplay = listOf("ど"),
      strokeCount = 3,
      strokeDescriptions = listOf(
        "1. Upper shorter horizontal stroke",
        "2. Central vertical stroke going straight down",
        "3. Bottom longer horizontal base stroke"
      )
    ),
    Kanji(
      number = 15,
      character = "子",
      meaning = "Child",
      level = "N5",
      kunReadings = listOf("こ"),
      onReadings = listOf("シ"),
      onReadingsDisplay = listOf("し"),
      strokeCount = 3,
      strokeDescriptions = listOf(
        "1. Top horizontal stroke turning sharply inward-left",
        "2. Curved vertical body stroke ending with an upward-left hook",
        "3. Horizontal arm stroke crossing through the middle"
      )
    ),
    Kanji(
      number = 16,
      character = "女",
      meaning = "Woman · Female",
      level = "N5",
      kunReadings = listOf("おんな"),
      onReadings = listOf("ジョ"),
      onReadingsDisplay = listOf("じょ"),
      strokeCount = 3,
      strokeDescriptions = listOf(
        "1. Sweeping stroke curving down-left then angled down-right",
        "2. Curved diagonal stroke sweeping from top-right to bottom-left",
        "3. Long horizontal stroke crossing through the top"
      )
    ),
    Kanji(
      number = 17,
      character = "学",
      meaning = "Study",
      level = "N5",
      kunReadings = listOf("（まな-ぶ）"),
      onReadings = listOf("ガク", "ガッ-"),
      onReadingsDisplay = listOf("がく", "がっ-"),
      strokeCount = 8,
      strokeDescriptions = listOf(
        "1. Top-left small downward dot",
        "2. Top-center small downward dot",
        "3. Top-right small inward flick",
        "4. Left vertical corner drop of the crown",
        "5. Top horizontal crown turning down-left on the right",
        "6. Top horizontal-angled stroke of 子 inside",
        "7. Curved vertical stem with a hook at the bottom",
        "8. Long horizontal crossbar across the bottom"
      )
    ),
    Kanji(
      number = 18,
      character = "生",
      meaning = "Live · Birth",
      level = "N5",
      kunReadings = listOf("い-きる", "う-まれる"),
      onReadings = listOf("セイ"),
      onReadingsDisplay = listOf("せい"),
      strokeCount = 5,
      strokeDescriptions = listOf(
        "1. Upper-left short diagonal sweep down-left",
        "2. Top horizontal stroke",
        "3. Central vertical stroke going straight down",
        "4. Middle shorter horizontal stroke",
        "5. Bottom long horizontal base stroke"
      )
    ),
    Kanji(
      number = 19,
      character = "先",
      meaning = "Previous · Foregoing",
      level = "N5",
      kunReadings = listOf("さき"),
      onReadings = listOf("セン"),
      onReadingsDisplay = listOf("せん"),
      strokeCount = 6,
      strokeDescriptions = listOf(
        "1. Top-left short diagonal sweep",
        "2. Upper horizontal stroke",
        "3. Central vertical stroke going down",
        "4. Long middle horizontal base stroke",
        "5. Lower-left curved leg sweeping down-left",
        "6. Lower-right leg going down, curving right, and hooking upward"
      )
    ),
    Kanji(
      number = 20,
      character = "私",
      meaning = "Private",
      level = "N5",
      kunReadings = listOf("わたし", "わたくし"),
      onReadings = listOf("シ"),
      onReadingsDisplay = listOf("し"),
      strokeCount = 7,
      strokeDescriptions = listOf(
        "1. Left-side top short diagonal sweep right to left",
        "2. Left-side horizontal crossbar",
        "3. Left-side vertical stem going down",
        "4. Left-side downward-left diagonal leaf",
        "5. Left-side small dot to the right",
        "6. Right-side angled stroke curving down-left then sweeping right",
        "7. Right-side closing dot at bottom-right"
      )
    )
  )

  val lesson3Kanji: List<Kanji> = listOf(
    Kanji(21, "一", "One", "N5", listOf("ひと・つ"), listOf("イチ", "イツ"), listOf("いち", "いつ"), 1),
    Kanji(22, "二", "Two", "N5", listOf("ふた・つ"), listOf("ニ"), listOf("に"), 2),
    Kanji(23, "三", "Three", "N5", listOf("みっ・つ"), listOf("サン"), listOf("さん"), 3),
    Kanji(24, "四", "Four", "N5", listOf("よっ・つ", "よん"), listOf("シ"), listOf("し"), 5),
    Kanji(25, "五", "Five", "N5", listOf("いつ・つ"), listOf("ゴ"), listOf("ご"), 4),
    Kanji(26, "六", "Six", "N5", listOf("むっ・つ"), listOf("ロク"), listOf("ろく"), 4),
    Kanji(27, "七", "Seven", "N5", listOf("なな・つ"), listOf("シチ"), listOf("しち"), 2),
    Kanji(28, "八", "Eight", "N5", listOf("やっ・つ"), listOf("ハチ"), listOf("はち"), 2),
    Kanji(29, "九", "Nine", "N5", listOf("ここの・つ"), listOf("キュウ", "ク"), listOf("きゅう", "く"), 2),
    Kanji(30, "十", "Ten", "N5", listOf("とお", "と"), listOf("ジュウ"), listOf("じゅう"), 2)
  )

  val n4Lesson1Kanji: List<Kanji> = listOf(
    Kanji(31, "春", "Spring", "N4", listOf("はる"), listOf("シュン"), listOf("しゅん"), 9),
    Kanji(32, "夏", "Summer", "N4", listOf("なつ"), listOf("カ", "ゲ"), listOf("か", "げ"), 10),
    Kanji(33, "秋", "Autumn · Fall", "N4", listOf("あき"), listOf("シュウ"), listOf("しゅう"), 9),
    Kanji(34, "冬", "Winter", "N4", listOf("ふゆ"), listOf("トウ"), listOf("とう"), 5),
    Kanji(35, "朝", "Morning", "N4", listOf("あさ"), listOf("チョウ"), listOf("ちょう"), 12),
    Kanji(36, "昼", "Daytime · Noon", "N4", listOf("ひる"), listOf("チュウ"), listOf("ちゅう"), 9),
    Kanji(37, "夕", "Evening", "N4", listOf("ゆう"), listOf("セキ"), listOf("せき"), 3),
    Kanji(38, "夜", "Night", "N4", listOf("よる", "よ"), listOf("ヤ"), listOf("や"), 8),
    Kanji(39, "週", "Week", "N4", listOf(), listOf("シュウ"), listOf("しゅう"), 11),
    Kanji(40, "曜", "Day of the week", "N4", listOf(), listOf("ヨウ"), listOf("よう"), 18)
  )

  val n3Lesson1Kanji: List<Kanji> = listOf(
    Kanji(41, "政", "Politics · Rule", "N3", listOf("まつりごと"), listOf("セイ", "ショウ"), listOf("せい", "しょう"), 9),
    Kanji(42, "経", "Pass through · Manage", "N3", listOf("へ・る"), listOf("ケイ", "キョウ"), listOf("けい", "きょう"), 11),
    Kanji(43, "済", "Settle · Finish", "N3", listOf("す・む"), listOf("サイ"), listOf("さい"), 11),
    Kanji(44, "議", "Deliberation · Discuss", "N3", listOf(), listOf("ギ"), listOf("ぎ"), 20),
    Kanji(45, "民", "People · Citizens", "N3", listOf("たみ"), listOf("ミン"), listOf("みん"), 5)
  )

  val n2Lesson1Kanji: List<Kanji> = listOf(
    Kanji(46, "届", "Deliver · Reach", "N2", listOf("とど・ける", "とど・く"), listOf("カイ"), listOf("かい"), 8),
    Kanji(47, "層", "Layer · Stratum", "N2", listOf(), listOf("ソウ"), listOf("そう"), 14),
    Kanji(48, "庁", "Government Office", "N2", listOf(), listOf("チョウ"), listOf("ちょう"), 5),
    Kanji(49, "録", "Record · Copy", "N2", listOf(), listOf("ロク"), listOf("ろく"), 16),
    Kanji(50, "省", "Ministry · Omit · Reflect", "N2", listOf("かえり・みる", "はぶ・く"), listOf("ショウ", "セイ"), listOf("しょう", "せい"), 9)
  )

  val n1Lesson1Kanji: List<Kanji> = listOf(
    Kanji(51, "憲", "Constitution · Law", "N1", listOf(), listOf("ケン"), listOf("けん"), 16),
    Kanji(52, "批", "Criticize · Compare", "N1", listOf(), listOf("ヒ"), listOf("ひ"), 7),
    Kanji(53, "盟", "Alliance · Oath", "N1", listOf(), listOf("メイ"), listOf("めい"), 13),
    Kanji(54, "閣", "Cabinet · Tower", "N1", listOf(), listOf("カク"), listOf("かく"), 14),
    Kanji(55, "譲", "Yield · Hand over", "N1", listOf("ゆず・る"), listOf("ジョウ"), listOf("じょう"), 20)
  )

  private val builtInLessons: List<KanjiLesson> = listOf(
    KanjiLesson(
      id = "N5_L1",
      lessonNumber = 1,
      title = "Lesson 1",
      subtitle = "Nature & Pictographs",
      level = "N5",
      kanjiList = lesson1Kanji
    ),
    KanjiLesson(
      id = "N5_L2",
      lessonNumber = 2,
      title = "Lesson 2",
      subtitle = "Elements & People",
      level = "N5",
      kanjiList = lesson2Kanji
    ),
    KanjiLesson(
      id = "N5_L3",
      lessonNumber = 3,
      title = "Lesson 3",
      subtitle = "Numbers & Counting",
      level = "N5",
      kanjiList = lesson3Kanji
    ),
    KanjiLesson(
      id = "N4_L1",
      lessonNumber = 1,
      title = "Lesson 1",
      subtitle = "Seasons & Time of Day",
      level = "N4",
      kanjiList = n4Lesson1Kanji
    ),
    KanjiLesson(
      id = "N3_L1",
      lessonNumber = 1,
      title = "Lesson 1",
      subtitle = "Society & Economy",
      level = "N3",
      kanjiList = n3Lesson1Kanji
    ),
    KanjiLesson(
      id = "N2_L1",
      lessonNumber = 1,
      title = "Lesson 1",
      subtitle = "Administration & Records",
      level = "N2",
      kanjiList = n2Lesson1Kanji
    ),
    KanjiLesson(
      id = "N1_L1",
      lessonNumber = 1,
      title = "Lesson 1",
      subtitle = "Governance & Law",
      level = "N1",
      kanjiList = n1Lesson1Kanji
    )
  )

  @Volatile
  private var remoteLessons: List<KanjiLesson> = emptyList()

  @Volatile
  private var remoteVocabularyByKanji: Map<Int, List<Vocabulary>> = emptyMap()

  @Volatile
  private var remoteStories: List<KanjiStory>? = null

  @Volatile
  private var remoteReadingExercises: List<KanjiReadingExercise>? = null

  fun applyRemoteSnapshot(snapshot: KanjiRemoteSnapshot) {
    if (snapshot.lessons.isNotEmpty()) {
      remoteLessons = snapshot.lessons
    }
    if (snapshot.vocabulary.isNotEmpty()) {
      remoteVocabularyByKanji = snapshot.vocabulary.groupBy { it.kanjiNumber }
    }
    if (snapshot.stories != null) {
      remoteStories = snapshot.stories
    }
    if (snapshot.readingExercises != null) {
      remoteReadingExercises = snapshot.readingExercises
    }
    if (snapshot.strokesByCharacter.isNotEmpty() || snapshot.guidanceByCharacter.isNotEmpty()) {
      com.example.ui.components.StrokeDataProvider.applyRemoteStrokeData(
        snapshot.strokesByCharacter,
        snapshot.guidanceByCharacter
      )
    }
  }

  val allLessons: List<KanjiLesson>
    get() {
      if (remoteLessons.isEmpty()) return builtInLessons
      val mergedById = LinkedHashMap<String, KanjiLesson>()
      builtInLessons.forEach { mergedById[it.id] = it }
      remoteLessons.forEach { remote ->
        val existing = mergedById[remote.id]
        // Preserve built-in stroke descriptions when remote JSON omits them
        val enrichedKanji = remote.kanjiList.map { rk ->
          if (rk.strokeDescriptions.isNotEmpty()) {
            rk
          } else {
            val fallbackDesc = existing?.kanjiList?.find { it.number == rk.number }?.strokeDescriptions.orEmpty()
            rk.copy(strokeDescriptions = fallbackDesc)
          }
        }
        mergedById[remote.id] = remote.copy(kanjiList = enrichedKanji)
      }
      val levelOrder = mapOf("N5" to 1, "N4" to 2, "N3" to 3, "N2" to 4, "N1" to 5)
      return mergedById.values.sortedWith(
        compareBy<KanjiLesson> { levelOrder[it.level] ?: 99 }.thenBy { it.lessonNumber }
      )
    }

  val allKanjiAcrossLevels: List<Kanji>
    get() = allLessons.flatMap { it.kanjiList }

  val lesson1Vocabulary: List<Vocabulary> = listOf(
    // Kanji 1: 日
    Vocabulary(1, "日", "ひ", "the sun, a day"),
    Vocabulary(1, "日本", "に・ほん／にっ・ぽん", "Japan"),
    Vocabulary(1, "日曜日", "にち・よう・び", "Sunday"),
    Vocabulary(1, "３日", "みっ・か", "the third day"),

    // Kanji 2: 月
    Vocabulary(2, "月", "つき", "the moon, a month"),
    Vocabulary(2, "月曜日", "げつ・よう・び", "Monday"),
    Vocabulary(2, "１月", "いち・がつ", "January"),
    Vocabulary(2, "１か月", "いっ・か・げつ", "one month"),

    // Kanji 3: 木
    Vocabulary(3, "木", "き", "a tree"),
    Vocabulary(3, "木曜日", "もく・よう・び", "Thursday"),
    Vocabulary(3, "木村", "き・むら", "Japanese name"),

    // Kanji 4: 山
    Vocabulary(4, "山", "やま", "a mountain"),
    Vocabulary(4, "富士山", "ふ・じ・さん", "Mt. Fuji"),
    Vocabulary(4, "山下", "やま・した", "Japanese name"),
    Vocabulary(4, "火山", "か・ざん", "a volcano"),

    // Kanji 5: 川
    Vocabulary(5, "川", "かわ", "a river"),
    Vocabulary(5, "ナイル川", "ないる・がわ", "the Nile River"),

    // Kanji 6: 田
    Vocabulary(6, "田んぼ", "たんぼ", "a rice field"),
    Vocabulary(6, "山田", "やま・だ", "Japanese name"),
    Vocabulary(6, "田中", "た・なか", "Japanese name"),

    // Kanji 7: 人
    Vocabulary(7, "人", "ひと", "man, a person"),
    Vocabulary(7, "３人", "さん・にん", "three people"),
    Vocabulary(7, "日本人", "に・ほん・じん", "a Japanese person"),

    // Kanji 8: 口
    Vocabulary(8, "口", "くち", "a mouth"),
    Vocabulary(8, "入口", "いり・ぐち", "an entrance"),
    Vocabulary(8, "出口", "で・ぐち", "an exit"),
    Vocabulary(8, "人口", "じん・こう", "population"),

    // Kanji 9: 車
    Vocabulary(9, "車", "くるま", "a car"),
    Vocabulary(9, "自動車", "じ・どう・しゃ", "a car, an automobile"),
    Vocabulary(9, "電車", "でん・しゃ", "a train"),

    // Kanji 10: 門
    Vocabulary(10, "門", "もん", "a gate"),
    Vocabulary(10, "専門", "せん・もん", "a specialty, major"),

    // Lesson 2 Vocabulary (Basic Kanji Book Lesson 2 — exact book examples)
    // Kanji 11: 火
    Vocabulary(11, "火", "ひ", "fire"),
    Vocabulary(11, "火山", "か・ざん", "a volcano"),
    Vocabulary(11, "火曜日", "か・よう・び", "Tuesday"),

    // Kanji 12: 水
    Vocabulary(12, "水", "みず", "water"),
    Vocabulary(12, "水田", "すい・でん", "a rice field"),
    Vocabulary(12, "水曜日", "すい・よう・び", "Wednesday"),

    // Kanji 13: 金
    Vocabulary(13, "お金", "お・かね", "money"),
    Vocabulary(13, "金田", "かね・だ", "Japanese name"),
    Vocabulary(13, "金", "きん", "gold"),
    Vocabulary(13, "金曜日", "きん・よう・び", "Friday"),

    // Kanji 14: 土
    Vocabulary(14, "土", "つち", "the ground"),
    Vocabulary(14, "土木", "ど・ぼく", "civil engineering"),
    Vocabulary(14, "土曜日", "ど・よう・び", "Saturday"),

    // Kanji 15: 子
    Vocabulary(15, "子ども", "こ・ども", "a child"),
    Vocabulary(15, "女の子", "おんな・の・こ", "a girl"),
    Vocabulary(15, "女子学生", "じょ・し・がく・せい", "a girl student"),

    // Kanji 16: 女
    Vocabulary(16, "女", "おんな", "a woman"),
    Vocabulary(16, "女の人", "おんな・の・ひと", "a woman"),
    Vocabulary(16, "彼女", "かの・じょ", "she"),

    // Kanji 17: 学
    Vocabulary(17, "学生", "がく・せい", "a student"),
    Vocabulary(17, "大学", "だい・がく", "a university"),
    Vocabulary(17, "学校", "がっ・こう", "a school"),

    // Kanji 18: 生
    Vocabulary(18, "生きる", "い・きる", "to live"),
    Vocabulary(18, "生まれる", "う・まれる", "to be born"),
    Vocabulary(18, "先生", "せん・せい", "a teacher"),
    Vocabulary(18, "学生", "がく・せい", "a student"),

    // Kanji 19: 先
    Vocabulary(19, "先", "さき", "ahead"),
    Vocabulary(19, "先月", "せん・げつ", "last month"),
    Vocabulary(19, "先生", "せん・せい", "a teacher"),

    // Kanji 20: 私
    Vocabulary(20, "私", "わたし／わたくし", "I"),
    Vocabulary(20, "私立大学", "し・りつ・だい・がく", "a private university"),

    // Lesson 3 Vocabulary
    Vocabulary(21, "一つ", "ひと・つ", "one thing"),
    Vocabulary(22, "二つ", "ふた・つ", "two things"),
    Vocabulary(23, "三つ", "みっ・つ", "three things"),
    Vocabulary(24, "四つ", "よっ・つ", "four things"),
    Vocabulary(25, "五つ", "いつ・つ", "five things"),
    Vocabulary(26, "六つ", "むっ・つ", "six things"),
    Vocabulary(27, "七つ", "なな・つ", "seven things"),
    Vocabulary(28, "八つ", "やっ・つ", "eight things"),
    Vocabulary(29, "九つ", "ここの・つ", "nine things"),
    Vocabulary(30, "十", "とお", "ten")
  )

  fun getVocabularyForKanji(kanjiNumber: Int): List<Vocabulary> {
    val remoteFound = remoteVocabularyByKanji[kanjiNumber]
    if (!remoteFound.isNullOrEmpty()) return remoteFound
    val found = lesson1Vocabulary.filter { it.kanjiNumber == kanjiNumber }
    if (found.isNotEmpty()) return found
    val k = allKanjiAcrossLevels.find { it.number == kanjiNumber } ?: return emptyList()
    val reading = k.kunReadings.firstOrNull() ?: k.onReadingsDisplay.firstOrNull() ?: ""
    return listOf(Vocabulary(kanjiNumber, k.character, reading, k.meaning.lowercase()))
  }

  fun getLessonsForLevel(level: String): List<KanjiLesson> {
    return allLessons.filter { it.level == level }
  }

  // Sentence reading material will be added later when provided by the user (or synced via Firebase Hosting)
  private val builtInReadingExercises: List<KanjiReadingExercise> = emptyList()

  val readingExercises: List<KanjiReadingExercise>
    get() = remoteReadingExercises ?: builtInReadingExercises

  // Authentic book story from Basic Kanji Book Lesson 5: プラニーさんのはなし
  private val builtInStories: List<KanjiStory> = listOf(
    KanjiStory(
      id = "N5_L5_STORY_1",
      level = "N5",
      lessonNumber = 5,
      title = "プラニーさんのはなし",
      fullPlainParagraphs = listOf(
        "私は日本大学の学生です。専門は体育です。\n十月九日にタイからきました。",
        "月曜日から金曜日まで日本語のクラスで\nべんきょうします。私は日本語が好きです。",
        "先週の土曜日に私たちは田中先生と山へ\nいきました。いろいろな学生がいっしょに\nきました。男子学生と女子学生が三人ずつ\nでした。私たちは岩田さんの車でいきました。",
        "岩田さんは日本人の男の人です。一時間ぐらい\nあるいて大きい森へいきました。そして、\n小さい川のちかくで休みました。その川の水は\nとてもきれいでした。"
      ),
      pages = listOf(
        KanjiStoryPage(
          pageNumber = 1,
          plainParagraphs = listOf(
            "私は日本大学の学生です。専門は体育です。\n十月九日にタイからきました。",
            "月曜日から金曜日まで日本語のクラスで\nべんきょうします。私は日本語が好きです。"
          ),
          furiganaLines = listOf(
            listOf(
              KanjiStoryToken("私は", "わたし は"),
              KanjiStoryToken("日本大学の学生です。", "にほんだいがく の がくせい です。"),
              KanjiStoryToken("専門は", "せんもん は"),
              KanjiStoryToken("体育です。", "たいいく です。")
            ),
            listOf(
              KanjiStoryToken("十月九日に", "じゅうがつ ここのか に"),
              KanjiStoryToken("タイから", "たい から"),
              KanjiStoryToken("きました。", "きました。")
            ),
            listOf(
              KanjiStoryToken("月曜日から", "げつようび から"),
              KanjiStoryToken("金曜日まで", "きんようび まで"),
              KanjiStoryToken("日本語のクラスで", "にほんご の クラス で")
            ),
            listOf(
              KanjiStoryToken("べんきょうします。", "べんきょう します。"),
              KanjiStoryToken("私は", "わたし は"),
              KanjiStoryToken("日本語が", "にほんご が"),
              KanjiStoryToken("好きです。", "すき です。")
            )
          ),
          englishLines = listOf(
            "I am a student at a Japanese university.",
            "My specialty is physical education.",
            "I came from Thailand on October 9.",
            "From Monday to Friday, I study Japanese in a class.",
            "I like Japanese."
          )
        ),
        KanjiStoryPage(
          pageNumber = 2,
          plainParagraphs = listOf(
            "先週の土曜日に私たちは田中先生と山へ\nいきました。いろいろな学生がいっしょに\nきました。男子学生と女子学生が三人ずつ\nでした。私たちは岩田さんの車でいきました。",
            "岩田さんは日本人の男の人です。一時間ぐらい\nあるいて大きい森へいきました。そして、\n小さい川のちかくで休みました。その川の水は\nとてもきれいでした。"
          ),
          furiganaLines = listOf(
            listOf(
              KanjiStoryToken("先週の土曜日に", "せんしゅう の どようび に"),
              KanjiStoryToken("私たちは", "わたしたち は"),
              KanjiStoryToken("田中先生と山へ", "たなかせんせい と やま へ")
            ),
            listOf(
              KanjiStoryToken("いきました。", "いきました。"),
              KanjiStoryToken("いろいろな学生が", "いろいろな がくせい が"),
              KanjiStoryToken("いっしょにきました。", "いっしょ に きました。")
            ),
            listOf(
              KanjiStoryToken("男子学生と", "だんしがくせい と"),
              KanjiStoryToken("女子学生が", "じょしがくせい が"),
              KanjiStoryToken("三人ずつでした。", "さんにん ずつ でした。")
            ),
            listOf(
              KanjiStoryToken("私たちは", "わたしたち は"),
              KanjiStoryToken("岩田さんの車で", "いわたさん の くるま で"),
              KanjiStoryToken("いきました。", "いきました。")
            ),
            listOf(
              KanjiStoryToken("岩田さんは", "いわたさん は"),
              KanjiStoryToken("日本人の", "にほんじん の"),
              KanjiStoryToken("男の人です。", "おとこのひと です。")
            ),
            listOf(
              KanjiStoryToken("一時間ぐらいあるいて", "いちじかん ぐらい あるいて"),
              KanjiStoryToken("大きい森へ", "おおきい もり へ"),
              KanjiStoryToken("いきました。", "いきました。")
            ),
            listOf(
              KanjiStoryToken("そして、", "そして、"),
              KanjiStoryToken("小さい川のちかくで", "ちいさい かわ の ちかく で"),
              KanjiStoryToken("休みました。", "やすみました。")
            ),
            listOf(
              KanjiStoryToken("その川の水は", "その かわ の みず は"),
              KanjiStoryToken("とてもきれいでした。", "とても きれい でした。")
            )
          ),
          englishLines = listOf(
            "Last Saturday, we went to the mountains with Mr. Tanaka.",
            "Various students came together.",
            "There were three male students and three female students each.",
            "We went in Mr. Iwata's car.",
            "Mr. Iwata is a Japanese man.",
            "We walked for about one hour and went to a big forest.",
            "Then, we rested near a small river.",
            "The water of that river was very clean."
          )
        )
      ),
      vocabulary = listOf(
        KanjiStoryVocab("専門（せんもん）", "speciality", "せんもん"),
        KanjiStoryVocab("体育（たいいく）", "physical exercises", "たいいく"),
        KanjiStoryVocab("曜日（ようび）", "day of the week", "ようび"),
        KanjiStoryVocab("〜語（〜ご）", "a language", "ご"),
        KanjiStoryVocab("〜が好きです", "I like ~", "がすきです"),
        KanjiStoryVocab("先週（せんしゅう）", "last week", "せんしゅう"),
        KanjiStoryVocab("私たち", "We", "わたしたち"),
        KanjiStoryVocab("いろいろな", "various", "いろいろな"),
        KanjiStoryVocab("〜ずつ", "~ for each", "ずつ"),
        KanjiStoryVocab("一時間（いちじかん）", "for one hour", "いちじかん"),
        KanjiStoryVocab("〜のちかくで", "near ~", "のちかくで")
      )
    )
  )

  val stories: List<KanjiStory>
    get() = remoteStories?.takeIf { it.isNotEmpty() } ?: builtInStories

  fun getReadingExercisesForLevel(level: String): List<KanjiReadingExercise> {
    return readingExercises.filter { it.level == level }
  }

  fun getStoriesForLevel(level: String): List<KanjiStory> {
    return stories.filter { it.level == level }
  }

  fun getReadingLessonsForLevel(level: String): List<KanjiLesson> {
    val baseLessons = getLessonsForLevel(level)
    val existingNumbers = baseLessons.map { it.lessonNumber }.toSet()
    val extraStoryLessons = stories
      .filter { it.level == level && !existingNumbers.contains(it.lessonNumber) }
      .groupBy { it.lessonNumber }
      .map { (lessonNum, lessonStories) ->
        KanjiLesson(
          id = "${level}_L$lessonNum",
          lessonNumber = lessonNum,
          title = "Lesson $lessonNum",
          subtitle = lessonStories.first().title,
          level = level,
          kanjiList = emptyList()
        )
      }
    val extraSentenceLessonNumbers = readingExercises
      .filter { it.level == level && !existingNumbers.contains(it.lessonNumber) && extraStoryLessons.none { s -> s.lessonNumber == it.lessonNumber } }
      .map { it.lessonNumber }
      .distinct()
      .map { lessonNum ->
        KanjiLesson(
          id = "${level}_L$lessonNum",
          lessonNumber = lessonNum,
          title = "Lesson $lessonNum",
          subtitle = "Sentence Reading",
          level = level,
          kanjiList = emptyList()
        )
      }
    return (baseLessons + extraStoryLessons + extraSentenceLessonNumbers).sortedBy { it.lessonNumber }
  }
}
