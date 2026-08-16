package com.flashcards.vocab.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flashcards.vocab.FlashcardsApp
import com.flashcards.vocab.ui.deckdetail.DeckDetailScreen
import com.flashcards.vocab.ui.decklist.DeckListScreen
import com.flashcards.vocab.ui.importdeck.ImportScreen
import com.flashcards.vocab.ui.review.ReviewScreen
import com.flashcards.vocab.ui.theme.FlashcardsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as FlashcardsApp).container.repository

        setContent {
            FlashcardsTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Routes.DECK_LIST) {
                    composable(Routes.DECK_LIST) {
                        DeckListScreen(
                            repository = repository,
                            onDeckClick = { deckId -> navController.navigate(Routes.deckDetail(deckId)) },
                            onImportNewDeck = { navController.navigate(Routes.import()) }
                        )
                    }
                    composable(
                        Routes.DECK_DETAIL,
                        arguments = listOf(navArgument("deckId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
                        DeckDetailScreen(
                            deckId = deckId,
                            repository = repository,
                            onStartReview = { navController.navigate(Routes.review(deckId)) },
                            onAddCards = { navController.navigate(Routes.import(deckId)) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        Routes.REVIEW,
                        arguments = listOf(navArgument("deckId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
                        ReviewScreen(
                            deckId = deckId,
                            repository = repository,
                            onFinish = { navController.popBackStack() }
                        )
                    }
                    composable(
                        Routes.IMPORT,
                        arguments = listOf(navArgument("deckId") { type = NavType.LongType; defaultValue = -1L })
                    ) { backStackEntry ->
                        val existingDeckId = backStackEntry.arguments?.getLong("deckId")?.takeIf { it > 0 }
                        ImportScreen(
                            existingDeckId = existingDeckId,
                            repository = repository,
                            onCancel = { navController.popBackStack() },
                            onDone = { savedDeckId ->
                                navController.popBackStack()
                                if (existingDeckId == null) {
                                    navController.navigate(Routes.deckDetail(savedDeckId)) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
