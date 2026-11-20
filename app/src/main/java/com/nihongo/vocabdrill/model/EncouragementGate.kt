package com.nihongo.vocabdrill.model

class EncouragementGate(private val choose: () -> Int = { listOf(5, 10, 15).random() }) {
    var shown = 0
        private set
    private var run = 0
    private var threshold = choose()
    fun answer(correct: Boolean, remaining: Int, enabled: Boolean): Boolean {
        if (!correct || !enabled) { run = 0; return false }
        run++
        if (shown >= 3 || remaining <= 0 || run < threshold) return false
        shown++; run = 0; threshold = choose()
        return true
    }
}
