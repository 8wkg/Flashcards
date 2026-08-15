package com.flashcards.vocab.ui.importdeck

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.flashcards.vocab.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flashcards.vocab.data.FlashcardRepository
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    existingDeckId: Long?,
    repository: FlashcardRepository,
    onDone: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ImportViewModel = viewModel(
        key = "import-${existingDeckId ?: -1}",
        factory = ViewModelFactory { ImportViewModel(existingDeckId, repository) }
    )
    val state by viewModel.uiState.collectAsState()

    var deckName by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Italian") }

    LaunchedEffect(state.savedDeckId) {
        state.savedDeckId?.let(onDone)
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val contents = context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }
        if (contents != null) {
            viewModel.onFileLoaded(queryFileName(context, uri) ?: "list.txt", contents)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingDeckId == null) "New deck" else "Add cards") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Upload a .txt or .csv file with one word pair per line, e.g. \"ciao,hello\".",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { filePicker.launch(arrayOf("text/*", "text/comma-separated-values", "text/csv")) }) {
                Text(state.fileName ?: "Choose file")
            }

            if (existingDeckId == null) {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Deck name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (state.parsedCards.isNotEmpty()) {
                Text("${state.parsedCards.size} cards found", style = MaterialTheme.typography.titleSmall)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.parsedCards) { card ->
                        ListItem(
                            headlineContent = { Text(card.front) },
                            supportingContent = { Text(card.back) }
                        )
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            Button(
                onClick = { viewModel.save(deckName.trim(), language.trim()) },
                enabled = state.parsedCards.isNotEmpty() &&
                    !state.isSaving &&
                    (existingDeckId != null || deckName.isNotBlank()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaving) "Saving..." else "Save deck")
            }
        }
    }
}

private fun queryFileName(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    return cursor.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) it.getString(index) else null
        } else {
            null
        }
    }
}
