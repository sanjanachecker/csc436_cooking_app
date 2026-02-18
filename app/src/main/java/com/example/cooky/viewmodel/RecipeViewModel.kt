package com.example.cooky.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cooky.data.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecipeViewModel : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe

    fun parseAndSetRecipe(rawRecipe: String) {
        // Simple parsing logic, assuming the recipe is well-formed.
        val lines = rawRecipe.lines()

        val title = lines.firstOrNull { it.isNotBlank() } ?: "Untitled Recipe"

        val ingredientsStartIndex = lines.indexOfFirst { it.equals("Ingredients", ignoreCase = true) }
        val stepsStartIndex = lines.indexOfFirst { it.equals("Instructions", ignoreCase = true) }

        val ingredients = if (ingredientsStartIndex != -1) {
            lines.subList(ingredientsStartIndex + 1, if (stepsStartIndex != -1) stepsStartIndex else lines.size)
                .filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        val steps = if (stepsStartIndex != -1) {
            lines.subList(stepsStartIndex + 1, lines.size)
                .filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        _recipe.value = Recipe(title, ingredients, steps)
    }
}
