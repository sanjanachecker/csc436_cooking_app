package com.example.cooky.data

import com.example.cooky.data.remote.ThemealdbApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(
    private val api: ThemealdbApi
) {
    /**
     * Returns a full set for selected letter; caller can cache per-letter results.
     */
    suspend fun fetchOnlineRecipesForLetter(letter: Char): List<Recipe> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.searchByFirstLetter(letter.toString())
            response.meals.orEmpty()
                .distinctBy { it.idMeal }
                .mapNotNull { meal ->
                    val title = meal.strMeal?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                    val ingredients = meal.toIngredientsList()
                    val steps = meal.toStepsList()
                    if (steps.isEmpty()) return@mapNotNull null
                    Recipe(
                        title = title,
                        ingredients = ingredients,
                        steps = steps,
                        category = meal.strCategory?.trim()?.takeIf { it.isNotEmpty() }
                    )
                }
        }.getOrElse { emptyList() }
    }

    suspend fun fetchCategories(): List<String> = withContext(Dispatchers.IO) {
        runCatching {
            api.listCategories().meals.orEmpty()
                .mapNotNull { it.strCategory?.trim()?.takeIf { c -> c.isNotEmpty() } }
        }.getOrElse { emptyList() }
    }
}
