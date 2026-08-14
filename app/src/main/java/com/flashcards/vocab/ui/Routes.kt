package com.flashcards.vocab.ui

object Routes {
    const val DECK_LIST = "deckList"
    const val DECK_DETAIL = "deckDetail/{deckId}"
    const val REVIEW = "review/{deckId}"
    const val IMPORT = "import?deckId={deckId}"

    fun deckDetail(deckId: Long) = "deckDetail/$deckId"
    fun review(deckId: Long) = "review/$deckId"
    fun import(deckId: Long? = null) = "import?deckId=${deckId ?: -1L}"
}
