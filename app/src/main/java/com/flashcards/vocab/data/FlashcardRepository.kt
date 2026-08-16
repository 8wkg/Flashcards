package com.flashcards.vocab.data

import com.flashcards.vocab.srs.Grade
import com.flashcards.vocab.srs.SpacedRepetitionScheduler
import kotlinx.coroutines.flow.Flow

class FlashcardRepository(
    private val deckDao: DeckDao,
    private val cardDao: CardDao
) {
    fun observeDecks(): Flow<List<Deck>> = deckDao.observeDecks()

    suspend fun getDeck(deckId: Long): Deck? = deckDao.getDeck(deckId)

    suspend fun createDeck(name: String, language: String): Long =
        deckDao.insert(Deck(name = name, language = language))

    suspend fun deleteDeck(deck: Deck) = deckDao.delete(deck)

    fun observeCards(deckId: Long): Flow<List<Card>> = cardDao.observeCardsForDeck(deckId)

    fun observeDueCount(deckId: Long, now: Long = System.currentTimeMillis()): Flow<Int> =
        cardDao.observeDueCount(deckId, now)

    suspend fun getDueCards(deckId: Long, now: Long = System.currentTimeMillis()): List<Card> =
        cardDao.getDueCards(deckId, now)

    suspend fun addCards(deckId: Long, entries: List<ParsedCard>) {
        cardDao.insertAll(
            entries.map { Card(deckId = deckId, front = it.front, back = it.back, notes = it.notes) }
        )
    }

    suspend fun gradeCard(card: Card, grade: Grade): Card {
        val updated = SpacedRepetitionScheduler.schedule(card, grade)
        cardDao.update(updated)
        return updated
    }

    suspend fun deleteCard(card: Card) = cardDao.delete(card)
}
