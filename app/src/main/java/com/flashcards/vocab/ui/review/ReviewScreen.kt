package com.flashcards.vocab.ui.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flashcards.vocab.ViewModelFactory
import com.flashcards.vocab.data.FlashcardRepository
import com.flashcards.vocab.srs.Grade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    deckId: Long,
    repository: FlashcardRepository,
    onFinish: () -> Unit
) {
    val viewModel: ReviewViewModel = viewModel(
        key = "review-$deckId",
        factory = ViewModelFactory { ReviewViewModel(deckId, repository) }
    )
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review") },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.isFinished -> FinishedContent(reviewedCount = state.reviewedCount, onDone = onFinish)
                else -> {
                    val card = state.currentCard!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                        Text(
                            "${state.reviewedCount + 1} reviewed · ${state.queue.size} left",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(Modifier.weight(1f))
                        FlashcardFace(
                            front = card.front,
                            back = card.back,
                            revealed = state.isRevealed,
                            onClick = { if (!state.isRevealed) viewModel.reveal() }
                        )
                        Spacer(Modifier.weight(1f))
                        if (state.isRevealed) {
                            GradeButtons(onGrade = viewModel::grade)
                        } else {
                            Button(onClick = viewModel::reveal, modifier = Modifier.fillMaxWidth()) {
                                Text("Show answer")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardFace(front: String, back: String, revealed: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 180f else 0f,
        animationSpec = tween(300),
        label = "flip"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick)
            .graphicsLayer { rotationY = rotation },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            if (rotation <= 90f) {
                Text(front, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            } else {
                Text(
                    back,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}

@Composable
private fun GradeButtons(onGrade: (Grade) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GradeButton("Again", MaterialTheme.colorScheme.error, Modifier.weight(1f)) { onGrade(Grade.AGAIN) }
        GradeButton("Hard", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { onGrade(Grade.HARD) }
        GradeButton("Good", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { onGrade(Grade.GOOD) }
        GradeButton("Easy", MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { onGrade(Grade.EASY) }
    }
}

@Composable
private fun GradeButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label)
    }
}

@Composable
private fun FinishedContent(reviewedCount: Int, onDone: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Nice work!", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("You reviewed $reviewedCount card${if (reviewedCount == 1) "" else "s"}.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text("Done") }
    }
}
