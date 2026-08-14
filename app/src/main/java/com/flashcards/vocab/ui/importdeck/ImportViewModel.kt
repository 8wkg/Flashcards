package com.flashcards.vocab.ui.importdeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.vocab.data.CardListImporter
import com.flashcards.vocab.data.FlashcardRepository
import com.flashcards.vocab.data.ParsedCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ImportUiState(
    val fileName: String? = null,
    val parsedCards: List<ParsedCard> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val savedDeckId: Long? = null
)

class ImportViewModel(
    private val existingDeckId: Long?,
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onFileLoaded(fileName: String, contents: String) {
        val parsed = CardListImporter.parse(contents)
        _uiState.value = _uiState.value.copy(
            fileName = fileName,
            parsedCards = parsed,
            error = if (parsed.isEmpty()) "No valid word pairs found. Each line should be \"word,translation\"." else null
        )
    }

    fun save(deckName: String, language: String) {
        val state = _uiState.value
        if (state.parsedCards.isEmpty()) return
        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            val deckId = existingDeckId ?: repository.createDeck(deckName, language)
            repository.addCards(deckId, state.parsedCards)
            _uiState.value = _uiState.value.copy(isSaving = false, savedDeckId = deckId)
        }
    }
}
