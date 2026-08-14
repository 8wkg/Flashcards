package com.flashcards.vocab.ui.deckdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.vocab.data.Card
import com.flashcards.vocab.data.Deck
import com.flashcards.vocab.data.FlashcardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeckDetailViewModel(
    private val deckId: Long,
    private val repository: FlashcardRepository
) : ViewModel() {

    val deck: StateFlow<Deck?> = flow { emit(repository.getDeck(deckId)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cards: StateFlow<List<Card>> = repository.observeCards(deckId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueCount: StateFlow<Int> = repository.observeDueCount(deckId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun deleteCard(card: Card) {
        viewModelScope.launch { repository.deleteCard(card) }
    }
}
