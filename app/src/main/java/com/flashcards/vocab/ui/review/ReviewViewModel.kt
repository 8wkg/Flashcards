package com.flashcards.vocab.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.vocab.data.Card
import com.flashcards.vocab.data.FlashcardRepository
import com.flashcards.vocab.srs.Grade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewUiState(
    val queue: List<Card> = emptyList(),
    val isRevealed: Boolean = false,
    val isLoading: Boolean = true,
    val reviewedCount: Int = 0
) {
    val currentCard: Card? get() = queue.firstOrNull()
    val isFinished: Boolean get() = !isLoading && queue.isEmpty()
}

class ReviewViewModel(
    deckId: Long,
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val due = repository.getDueCards(deckId)
            _uiState.value = ReviewUiState(queue = due, isLoading = false)
        }
    }

    fun reveal() {
        _uiState.value = _uiState.value.copy(isRevealed = true)
    }

    fun grade(grade: Grade) {
        val state = _uiState.value
        val card = state.currentCard ?: return
        viewModelScope.launch {
            val updated = repository.gradeCard(card, grade)
            val remaining = state.queue.drop(1).toMutableList()
            // Cards marked "Again" get another shot later in the same session.
            if (grade == Grade.AGAIN) {
                remaining.add(updated)
            }
            _uiState.value = state.copy(
                queue = remaining,
                isRevealed = false,
                reviewedCount = state.reviewedCount + 1
            )
        }
    }
}
