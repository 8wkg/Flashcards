package com.flashcards.vocab.ui.decklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.vocab.data.Deck
import com.flashcards.vocab.data.FlashcardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DeckListViewModel(private val repository: FlashcardRepository) : ViewModel() {
    val decks: StateFlow<List<Deck>> = repository.observeDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
