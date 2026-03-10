package com.example.cooky.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cooky.viewmodel.RecipeViewModel
import java.util.Locale

@Composable
fun ActiveCookingStepScreen(
    recipeViewModel: RecipeViewModel,
    onRecipeComplete: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val recipe by recipeViewModel.recipe.collectAsState()
    val isSpeaking by recipeViewModel.isSpeaking.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    recipe?.steps?.let { steps ->
        if (currentIndex >= steps.size) {
            onRecipeComplete()
            return
        }

        // Speak the current step when it changes
        LaunchedEffect(currentIndex) {
            recipeViewModel.speak(steps[currentIndex])
        }

        DisposableEffect(Unit) {
            onDispose { recipeViewModel.stopSpeaking() }
        }

        // Create recognizer once and hold it in remember
        // Create recognizer once
        val speechRecognizer = remember {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else null
        }

        // Single source of truth: should we be listening right now?
        var shouldListen by remember { mutableStateOf(false) }
        // Prevent overlapping startListening calls
        var isCurrentlyListening by remember { mutableStateOf(false) }

        DisposableEffect(Unit) {
            if (speechRecognizer == null) return@DisposableEffect onDispose {}

            val handler = android.os.Handler(android.os.Looper.getMainLooper())

            fun doStartListening() {
                if (!shouldListen || isCurrentlyListening) return
                isCurrentlyListening = true
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                }
                try {
                    speechRecognizer.startListening(intent)
                } catch (e: Exception) {
                    android.util.Log.d("SpeechDebug", "startListening exception: ${e.message}")
                    isCurrentlyListening = false
                    handler.postDelayed({ doStartListening() }, 1500)
                }
            }

            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    android.util.Log.d("SpeechDebug", "onReadyForSpeech")
                }
                override fun onBeginningOfSpeech() {
                    android.util.Log.d("SpeechDebug", "onBeginningOfSpeech")
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    android.util.Log.d("SpeechDebug", "onEndOfSpeech")
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}

                override fun onError(error: Int) {
                    android.util.Log.d("SpeechDebug", "onError code: $error")
                    isCurrentlyListening = false
                    // Don't retry if we shouldn't be listening
                    if (!shouldListen) return
                    // Longer delay to avoid hammering
                    val delay = when (error) {
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 2000L
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> 500L
                        else -> 1500L
                    }
                    handler.postDelayed({ doStartListening() }, delay)
                }

                override fun onResults(results: Bundle?) {
                    isCurrentlyListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    android.util.Log.d("SpeechDebug", "onResults matches: $matches")

                    if (matches.isNullOrEmpty()) {
                        handler.postDelayed({ doStartListening() }, 500)
                        return
                    }

                    val commandText = matches.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""
                    android.util.Log.d("SpeechDebug", "commandText: '$commandText'")

                    when {
                        "next" in commandText -> {
                            android.util.Log.d("SpeechDebug", "MATCHED: next")
                            recipeViewModel.stopSpeaking()
                            currentIndex++
                        }
                        "previous" in commandText || "back" in commandText -> {
                            android.util.Log.d("SpeechDebug", "MATCHED: previous/back")
                            recipeViewModel.stopSpeaking()
                            if (currentIndex > 0) currentIndex--
                        }
                        "repeat" in commandText || "again" in commandText -> {
                            android.util.Log.d("SpeechDebug", "MATCHED: repeat/again")
                            recipeViewModel.speak(steps[currentIndex])
                        }
                        else -> {
                            android.util.Log.d("SpeechDebug", "NO MATCH for: '$commandText'")
                        }
                    }
                    // Restart listening after a short pause
                    handler.postDelayed({ doStartListening() }, 500)
                }
            }

            speechRecognizer.setRecognitionListener(listener)

            onDispose {
                shouldListen = false
                isCurrentlyListening = false
                handler.removeCallbacksAndMessages(null)
                speechRecognizer.setRecognitionListener(null)
                speechRecognizer.stopListening()
                speechRecognizer.cancel()
                speechRecognizer.destroy()
            }
        }

        // Gate listening on TTS state
        LaunchedEffect(isSpeaking) {
            if (isSpeaking) {
                shouldListen = false
                isCurrentlyListening = false
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } else {
                kotlinx.coroutines.delay(800)
                shouldListen = true
                isCurrentlyListening = true
                speechRecognizer?.cancel()  // cancel any pending session first
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")

                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                }
                try {
                    speechRecognizer?.startListening(intent)
                } catch (_: Exception) {
                    isCurrentlyListening = false
                }
            }
        }

        // ---- UI (unchanged) ----
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(steps.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentIndex) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
                Text(
                    text = "Step ${currentIndex + 1} of ${steps.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = steps[currentIndex],
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (isSpeaking) "Listening paused while reading step aloud…"
                    else "Listening for voice commands: \"Next\", \"Previous\", or \"Repeat\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(28.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            recipeViewModel.stopSpeaking()
                            if (currentIndex > 0) currentIndex--
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Previous", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = { recipeViewModel.speak(steps[currentIndex]) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Repeat", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = {
                            recipeViewModel.stopSpeaking()
                            currentIndex++
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            TextButton(
                onClick = {
                    recipeViewModel.stopSpeaking()
                    onExit()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Text("Exit", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}