package com.example.cooky.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cooky.viewmodel.RecipeViewModel

@Composable
fun CookyNavHost() {
    val navController = rememberNavController()
    val recipeViewModel: RecipeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "import") {
        composable("import") {
            RecipeImportScreen(recipeViewModel) {
                navController.navigate("overview")
            }
        }
        composable("overview") {
            RecipeOverviewScreen(
                recipeViewModel = recipeViewModel,
                onStartCooking = { navController.navigate("cooking") },
                onViewIngredients = { navController.navigate("ingredients") }
            )
        }
        composable("ingredients") {
            IngredientsPreparationScreen(recipeViewModel) {
                navController.popBackStack()
            }
        }
        composable("cooking") {
            ActiveCookingStepScreen(recipeViewModel) {
                navController.navigate("completion")
            }
        }
        composable("completion") {
            CompletionScreen(
                onRestart = { navController.navigate("import") { popUpTo("import") { inclusive = true } } },
                onExit = { /* Add exit logic here */ }
            )
        }
    }
}
