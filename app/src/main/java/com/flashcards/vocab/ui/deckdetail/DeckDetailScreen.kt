package com.flashcards.vocab.ui.deckdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flashcards.vocab.ViewModelFactory
import com.flashcards.vocab.data.FlashcardRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: Long,
    repository: FlashcardRepository,
    onStartReview: () -> Unit,
    onAddCards: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: DeckDetailViewModel = viewModel(
        key = "deckDetail-$deckId",
        factory = ViewModelFactory { DeckDetailViewModel(deckId, repository) }
    )
    val deck by viewModel.deck.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val dueCount by viewModel.dueCount.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this deck?") },
            text = {
                Text(
                    "This will permanently delete \"${deck?.name}\" and all ${cards.size} " +
                        "card${if (cards.size == 1) "" else "s"} in it. This can't be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteDeck(onDeleted = onBack)
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deck?.name ?: "Deck") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete deck")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add cards") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onAddCards
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Button(
                onClick = onStartReview,
                enabled = dueCount > 0,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(if (dueCount > 0) "Study ($dueCount due)" else "No cards due")
            }
            HorizontalDivider()
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cards, key = { it.id }) { card ->
                    ListItem(
                        headlineContent = { Text(card.front) },
                        supportingContent = { Text(card.back) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteCard(card) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete card")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
