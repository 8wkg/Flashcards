package com.flashcards.vocab.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY name ASC")
    fun observeDecks(): Flow<List<Deck>>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeck(deckId: Long): Deck?

    @Insert
    suspend fun insert(deck: Deck): Long

    @Delete
    suspend fun delete(deck: Deck)
}
