package com.flashcards.vocab.srs

import com.flashcards.vocab.data.Card
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

/**
 * A simplified SM-2 style scheduler (as used by Anki/SuperMemo). New or lapsed cards move
 * through short "learning steps" measured in minutes; once graduated they use day-based
 * intervals that grow or shrink with each review based on the ease factor.
 */
object SpacedRepetitionScheduler {
    private val learningStepsMinutes = listOf(1L, 10L)
    private const val minEaseFactor = 1.3
    private const val graduatingIntervalDays = 1.0

    fun schedule(card: Card, grade: Grade, now: Long = System.currentTimeMillis()): Card {
        return if (card.repetitions < learningStepsMinutes.size) {
            scheduleLearning(card, grade, now)
        } else {
            scheduleReview(card, grade, now)
        }
    }

    private fun scheduleLearning(card: Card, grade: Grade, now: Long): Card = when (grade) {
        Grade.AGAIN -> card.copy(
            repetitions = 0,
            intervalDays = 0.0,
            dueAt = now + TimeUnit.MINUTES.toMillis(learningStepsMinutes.first()),
            lastReviewedAt = now
        )
        Grade.HARD -> card.copy(
            dueAt = now + TimeUnit.MINUTES.toMillis(learningStepsMinutes[card.repetitions]),
            lastReviewedAt = now
        )
        Grade.GOOD -> {
            val nextStep = card.repetitions + 1
            if (nextStep >= learningStepsMinutes.size) {
                graduate(card, now, graduatingIntervalDays)
            } else {
                card.copy(
                    repetitions = nextStep,
                    dueAt = now + TimeUnit.MINUTES.toMillis(learningStepsMinutes[nextStep]),
                    lastReviewedAt = now
                )
            }
        }
        Grade.EASY -> graduate(card, now, graduatingIntervalDays * 4)
    }

    private fun graduate(card: Card, now: Long, intervalDays: Double): Card = card.copy(
        repetitions = learningStepsMinutes.size,
        intervalDays = intervalDays,
        dueAt = now + daysToMillis(intervalDays),
        lastReviewedAt = now
    )

    private fun scheduleReview(card: Card, grade: Grade, now: Long): Card {
        val ease = card.easeFactor
        return when (grade) {
            Grade.AGAIN -> card.copy(
                repetitions = 0,
                easeFactor = (ease - 0.2).coerceAtLeast(minEaseFactor),
                intervalDays = 0.0,
                dueAt = now + TimeUnit.MINUTES.toMillis(learningStepsMinutes.first()),
                lastReviewedAt = now
            )
            Grade.HARD -> {
                val interval = (card.intervalDays * 1.2).coerceAtLeast(1.0)
                card.copy(
                    repetitions = card.repetitions + 1,
                    easeFactor = (ease - 0.15).coerceAtLeast(minEaseFactor),
                    intervalDays = interval,
                    dueAt = now + daysToMillis(interval),
                    lastReviewedAt = now
                )
            }
            Grade.GOOD -> {
                val interval = (card.intervalDays * ease).coerceAtLeast(1.0)
                card.copy(
                    repetitions = card.repetitions + 1,
                    intervalDays = interval,
                    dueAt = now + daysToMillis(interval),
                    lastReviewedAt = now
                )
            }
            Grade.EASY -> {
                val interval = (card.intervalDays * ease * 1.3).coerceAtLeast(1.0)
                card.copy(
                    repetitions = card.repetitions + 1,
                    easeFactor = ease + 0.15,
                    intervalDays = interval,
                    dueAt = now + daysToMillis(interval),
                    lastReviewedAt = now
                )
            }
        }
    }

    private fun daysToMillis(days: Double): Long = (days * TimeUnit.DAYS.toMillis(1)).roundToLong()
}
