package com.example.cooky.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cooky.data.Recipe
import com.example.cooky.data.RecipeRepository
import com.example.cooky.data.remote.ThemealdbApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val api: ThemealdbApi = Retrofit.Builder()
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ThemealdbApi::class.java)

    private val repository = RecipeRepository(api)

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    private val _onlineRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val onlineRecipes: StateFlow<List<Recipe>> = _onlineRecipes.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedLetter = MutableStateFlow<String?>(null)
    val selectedLetter: StateFlow<String?> = _selectedLetter.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val filteredOnlineRecipes: StateFlow<List<Recipe>> = combine(
        _onlineRecipes,
        _selectedLetter,
        _selectedCategory
    ) { recipes, letter, category ->
        recipes.filter { recipe ->
            (letter == null || recipe.title.getOrNull(0)?.lowercaseChar() == letter.single()) &&
            (category == null || recipe.category == category)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoadingOnline = MutableStateFlow(false)
    val isLoadingOnline: StateFlow<Boolean> = _isLoadingOnline.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                // Use media stream so TTS follows media volume and is easier to hear
                tts?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
            }
        }

        loadOnlineRecipes()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categories.value = repository.fetchCategories()
        }
    }

    private fun loadOnlineRecipes() {
        viewModelScope.launch {
            _isLoadingOnline.value = true
            val recipes = repository.fetchOnlineRecipes()
            _onlineRecipes.value = recipes
            _isLoadingOnline.value = false
        }
    }

    /** Call to retry loading online recipes (e.g. after network error). */
    fun refreshOnlineRecipes() {
        loadOnlineRecipes()
    }

    fun setLetterFilter(letter: String?) {
        _selectedLetter.value = letter
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
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
        tts?.stop()
        _isSpeaking.value = true
        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { _isSpeaking.value = false }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { _isSpeaking.value = false }
        })
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "cooky_utterance")
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
