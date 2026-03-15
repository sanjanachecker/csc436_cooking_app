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

    // Current letter whose recipes are loaded (drives fetching).
    private val _selectedLetter = MutableStateFlow<String>("a")
    val selectedLetter: StateFlow<String> = _selectedLetter.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Client-side category filter over the fully loaded set for the current letter.
    val filteredOnlineRecipes: StateFlow<List<Recipe>> = combine(
        _onlineRecipes,
        _selectedCategory
    ) { recipes, category ->
        recipes.filter { recipe ->
            category == null || recipe.category == category
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoadingOnline = MutableStateFlow(false)
    val isLoadingOnline: StateFlow<Boolean> = _isLoadingOnline.asStateFlow()

    // In-memory cache of recipes per starting letter for this app session.
    private val letterCache: MutableMap<Char, List<Recipe>> = mutableMapOf()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Global search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Recipe>>(emptyList())
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Recipes shown in the UI: either global search results or per-letter filtered list
    val displayedOnlineRecipes: StateFlow<List<Recipe>> = combine(
        searchQuery,
        _searchResults,
        filteredOnlineRecipes
    ) { query, search, byLetter ->
        if (query.isNotBlank()) search else byLetter
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

        // Initial load: recipes starting with 'A' and all categories.
        loadOnlineRecipesForCurrentLetter()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categories.value = repository.fetchCategories()
        }
    }

    private fun loadOnlineRecipesForCurrentLetter(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoadingOnline.value = true
            val letterChar = _selectedLetter.value.firstOrNull() ?: 'a'
            val cached = letterCache[letterChar]
            val recipes = if (!forceRefresh && cached != null) {
                cached
            } else {
                val fresh = repository.fetchOnlineRecipesForLetter(letterChar)
                letterCache[letterChar] = fresh
                fresh
            }
            _onlineRecipes.value = recipes
            _isLoadingOnline.value = false
        }
    }

    /** Call to retry loading online recipes (e.g. after network error). */
    fun refreshOnlineRecipes() {
        // Force a network fetch for the current letter and overwrite cache.
        loadOnlineRecipesForCurrentLetter(forceRefresh = true)
    }

    fun setLetterFilter(letter: String?) {
        val normalized = letter?.lowercase()?.takeIf { it.isNotEmpty() } ?: "a"
        if (_selectedLetter.value == normalized) return
        _selectedLetter.value = normalized
        loadOnlineRecipesForCurrentLetter()
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        val trimmed = query.trim()
        _searchQuery.value = trimmed

        if (trimmed.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = repository.searchRecipesByName(trimmed)
            _isSearching.value = false
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
