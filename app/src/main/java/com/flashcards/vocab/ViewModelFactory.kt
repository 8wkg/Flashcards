package com.flashcards.vocab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** A tiny [ViewModelProvider.Factory] for constructing view models that take manual DI args. */
class ViewModelFactory<VM : ViewModel>(private val creator: () -> VM) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}
