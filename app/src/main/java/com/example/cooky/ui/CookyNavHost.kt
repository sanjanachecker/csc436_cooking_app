package com.example.cooky.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cooky.data.hasCompletedOnboarding
import com.example.cooky.data.onboardingPreferences
import com.example.cooky.data.setOnboardingComplete
import com.example.cooky.viewmodel.RecipeViewModel

@Composable
fun CookyNavHost() {
    val navController = rememberNavController()
    val recipeViewModel: RecipeViewModel = viewModel()
    val onlineRecipes by recipeViewModel.onlineRecipes.collectAsState()
    val filteredOnlineRecipes by recipeViewModel.filteredOnlineRecipes.collectAsState()
    val categories by recipeViewModel.categories.collectAsState()
    val selectedLetter by recipeViewModel.selectedLetter.collectAsState()
    val selectedCategory by recipeViewModel.selectedCategory.collectAsState()
    val isLoadingOnline by recipeViewModel.isLoadingOnline.collectAsState()
    val context = LocalContext.current
    val prefs = remember { context.onboardingPreferences() }
    var hasCompletedOnboarding by remember { mutableStateOf(prefs.hasCompletedOnboarding()) }
    var searchQuery by remember { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = if (hasCompletedOnboarding) "recipe_picker" else "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(onComplete = {
                prefs.setOnboardingComplete()
                hasCompletedOnboarding = true
                navController.navigate("recipe_picker") { popUpTo("onboarding") { inclusive = true } }
            })
        }
        composable("recipe_picker") {
            RecipePickerScreen(
                onlineRecipes = filteredOnlineRecipes,
                totalOnlineCount = onlineRecipes.size,
                isLoadingOnline = isLoadingOnline,
                categories = categories,
                selectedLetter = selectedLetter,
                selectedCategory = selectedCategory,
                 searchQuery = searchQuery,
                 onSearchQueryChange = { searchQuery = it },
                onLetterFilter = { recipeViewModel.setLetterFilter(it) },
                onCategoryFilter = { recipeViewModel.setCategoryFilter(it) },
                onSelectRecipe = { recipe ->
                    recipeViewModel.setRecipe(recipe)
                    navController.navigate("overview")
                },
                onPasteOwn = { navController.navigate("import") },
                onHelp = { navController.navigate("help") },
                onRetryOnline = { recipeViewModel.refreshOnlineRecipes() }
            )
        }
        composable("help") {
            OnboardingScreen(onComplete = { navController.popBackStack() })
        }
        composable("import") {
            RecipeImportScreen(
                recipeViewModel = recipeViewModel,
                onRecipeAnalyzed = { navController.navigate("overview") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("overview") {
            RecipeOverviewScreen(
                recipeViewModel = recipeViewModel,
                onStartCooking = { navController.navigate("cooking") },
                onViewIngredients = { navController.navigate("ingredients") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("ingredients") {
            IngredientsPreparationScreen(
                recipeViewModel = recipeViewModel,
                onBackToRecipe = { navController.popBackStack() }
            )
        }
        composable("cooking") {
            ActiveCookingStepScreen(
                recipeViewModel = recipeViewModel,
                onRecipeComplete = { navController.navigate("completion") },
                onExit = {
                    navController.navigate("recipe_picker") {
                        popUpTo("recipe_picker") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("completion") {
            CompletionScreen(
                onBackToRecipes = {
                    navController.navigate("recipe_picker") {
                        popUpTo("recipe_picker") { inclusive = true }
                    }
                }
            )
        }
    }
}
