package com.flashcards.vocab.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY id ASC")
    fun observeCardsForDeck(deckId: Long): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND dueAt <= :now ORDER BY dueAt ASC")
    suspend fun getDueCards(deckId: Long, now: Long): List<Card>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND dueAt <= :now")
    fun observeDueCount(deckId: Long, now: Long): Flow<Int>

    @Insert
    suspend fun insertAll(cards: List<Card>)

    @Update
    suspend fun update(card: Card)

    @Delete
    suspend fun delete(card: Card)
}
