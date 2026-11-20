package com.nihongo.vocabdrill.model

/** Monotonic countdown: pausing preserves fractional seconds; scheduler delays cannot extend it. */
class RecallCountdown(private val now: () -> Long) {
    private var savedMillis = 0L
    private var deadline: Long? = null
    val remainingMillis: Long get() = deadline?.let { (it - now()).coerceAtLeast(0L) } ?: savedMillis
    val isRunning: Boolean get() = deadline != null
    fun reset(milliseconds: Long) { deadline = null; savedMillis = milliseconds.coerceAtLeast(0L) }
    fun pause() { savedMillis = remainingMillis; deadline = null }
    fun resume() { if (deadline == null && savedMillis > 0) deadline = now() + savedMillis }
}
