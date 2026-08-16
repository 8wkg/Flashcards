package com.flashcards.vocab

import android.content.Context
import com.flashcards.vocab.data.AppDatabase
import com.flashcards.vocab.data.FlashcardRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val repository = FlashcardRepository(database.deckDao(), database.cardDao())
}
