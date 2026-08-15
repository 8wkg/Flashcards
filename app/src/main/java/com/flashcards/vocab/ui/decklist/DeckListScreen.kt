package com.flashcards.vocab.ui.decklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flashcards.vocab.ViewModelFactory
import com.flashcards.vocab.data.Deck
import com.flashcards.vocab.data.FlashcardRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    repository: FlashcardRepository,
    onDeckClick: (Long) -> Unit,
    onImportNewDeck: () -> Unit
) {
    val viewModel: DeckListViewModel = viewModel(factory = ViewModelFactory { DeckListViewModel(repository) })
    val decks by viewModel.decks.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        InfoDialog(onDismiss = { showInfoDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Decks") },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "About this app")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Import list") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onImportNewDeck
            )
        }
    ) { padding ->
        if (decks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No decks yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap \"Import list\" to upload a word list and create your first deck.")
                }
            }
        } else {
            LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
                items(decks, key = { it.id }) { deck ->
                    DeckRow(deck = deck, repository = repository, onClick = { onDeckClick(deck.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DeckRow(deck: Deck, repository: FlashcardRepository, onClick: () -> Unit) {
    val dueCount by remember(deck.id) { repository.observeDueCount(deck.id) }.collectAsState(initial = 0)

    ListItem(
        headlineContent = { Text(deck.name, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(deck.language) },
        trailingContent = {
            if (dueCount > 0) {
                Badge { Text("$dueCount due") }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
        title = { Text("About Flashcards") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Flashcards helps you learn vocabulary in any language using flip cards and " +
                        "spaced repetition: after you grade how well you knew a card, it schedules " +
                        "when that card should come back — cards you find hard reappear sooner, " +
                        "cards you know well reappear later."
                )
                Text("Creating a deck", fontWeight = FontWeight.SemiBold)
                Text(
                    "Decks aren't typed in one card at a time — you upload a word list file " +
                        "instead, either to create a new deck or add to an existing one."
                )
                Text("File format", fontWeight = FontWeight.SemiBold)
                Text("• A plain .txt or .csv file")
                Text("• One word pair per line: word, translation")
                Text("• Comma, tab, or semicolon all work as the separator")
                Text("• An optional third column becomes a note")
                Text("• A header row like \"word,translation\" is detected and skipped automatically")
                Text("Example:", fontWeight = FontWeight.SemiBold)
                Text(
                    "ciao,hello\ngrazie,thank you\ncasa,house,noun",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}
