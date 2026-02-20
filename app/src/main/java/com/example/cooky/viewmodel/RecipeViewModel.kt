package com.example.cooky.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooky.data.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    fun setRecipe(recipe: Recipe) {
        _recipe.value = recipe
    }

    fun parseAndSetRecipe(rawRecipe: String) {
        val lines = rawRecipe.lines()
        val title = lines.firstOrNull { it.isNotBlank() } ?: "Untitled Recipe"
        val ingredientsStartIndex = lines.indexOfFirst { it.equals("Ingredients", ignoreCase = true) }
        val stepsStartIndex = lines.indexOfFirst { it.equals("Instructions", ignoreCase = true) }
        val ingredients = if (ingredientsStartIndex != -1) {
            lines.subList(ingredientsStartIndex + 1, if (stepsStartIndex != -1) stepsStartIndex else lines.size)
                .filter { it.isNotBlank() }
        } else emptyList()
        val steps = if (stepsStartIndex != -1) {
            lines.subList(stepsStartIndex + 1, lines.size).filter { it.isNotBlank() }
        } else emptyList()
        _recipe.value = Recipe(title, ingredients, steps)
    }

    fun speak(text: String) {
        viewModelScope.launch {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
