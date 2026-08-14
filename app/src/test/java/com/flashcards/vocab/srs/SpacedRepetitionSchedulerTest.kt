package com.flashcards.vocab.srs

import com.flashcards.vocab.data.Card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class SpacedRepetitionSchedulerTest {

    private val now = 1_700_000_000_000L
    private fun newCard() = Card(id = 1, deckId = 1, front = "ciao", back = "hello", dueAt = now)

    @Test
    fun `new card graded Again stays in the first learning step`() {
        val result = SpacedRepetitionScheduler.schedule(newCard(), Grade.AGAIN, now)
        assertEquals(0, result.repetitions)
        assertEquals(now + TimeUnit.MINUTES.toMillis(1), result.dueAt)
    }

    @Test
    fun `new card graded Good twice graduates to a one day interval`() {
        val afterFirst = SpacedRepetitionScheduler.schedule(newCard(), Grade.GOOD, now)
        assertEquals(1, afterFirst.repetitions)
        assertEquals(now + TimeUnit.MINUTES.toMillis(10), afterFirst.dueAt)

        val afterSecond = SpacedRepetitionScheduler.schedule(afterFirst, Grade.GOOD, now)
        assertEquals(2, afterSecond.repetitions)
        assertEquals(1.0, afterSecond.intervalDays, 0.0001)
        assertEquals(now + TimeUnit.DAYS.toMillis(1), afterSecond.dueAt)
    }

    @Test
    fun `new card graded Easy graduates immediately with a longer interval`() {
        val result = SpacedRepetitionScheduler.schedule(newCard(), Grade.EASY, now)
        assertEquals(2, result.repetitions)
        assertEquals(4.0, result.intervalDays, 0.0001)
    }

    @Test
    fun `review card graded Good grows the interval by the ease factor`() {
        val reviewCard = newCard().copy(repetitions = 2, intervalDays = 4.0, easeFactor = 2.5)
        val result = SpacedRepetitionScheduler.schedule(reviewCard, Grade.GOOD, now)
        assertEquals(10.0, result.intervalDays, 0.0001)
        assertEquals(2.5, result.easeFactor, 0.0001)
    }

    @Test
    fun `review card graded Again lapses back into learning and lowers ease`() {
        val reviewCard = newCard().copy(repetitions = 5, intervalDays = 20.0, easeFactor = 2.5)
        val result = SpacedRepetitionScheduler.schedule(reviewCard, Grade.AGAIN, now)
        assertEquals(0, result.repetitions)
        assertEquals(0.0, result.intervalDays, 0.0001)
        assertEquals(2.3, result.easeFactor, 0.0001)
        assertEquals(now + TimeUnit.MINUTES.toMillis(1), result.dueAt)
    }

    @Test
    fun `ease factor never drops below the floor`() {
        var card = newCard().copy(repetitions = 2, intervalDays = 4.0, easeFactor = 1.35)
        card = SpacedRepetitionScheduler.schedule(card, Grade.AGAIN, now)
        assertTrue(card.easeFactor >= 1.3)
    }
}
