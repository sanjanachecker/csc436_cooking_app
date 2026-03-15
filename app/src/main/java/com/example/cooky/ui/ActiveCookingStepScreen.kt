package com.example.cooky.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cooky.viewmodel.RecipeViewModel

@Composable
fun ActiveCookingStepScreen(
    recipeViewModel: RecipeViewModel,
    onRecipeComplete: () -> Unit,
    onBackToRecipes: () -> Unit
) {
    val recipe by recipeViewModel.recipe.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    recipe?.steps?.let { steps ->
        if (currentIndex >= steps.size) {
            onRecipeComplete()
            return
        }

        LaunchedEffect(currentIndex) {
            recipeViewModel.speak(steps[currentIndex])
        }

        DisposableEffect(Unit) {
            onDispose {
                recipeViewModel.stopSpeaking()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Step ${currentIndex + 1} of ${steps.size}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = steps[currentIndex],
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Steps are read aloud. Use the buttons to go back, repeat, or continue.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    recipeViewModel.stopSpeaking()
                    if (currentIndex > 0) currentIndex--
                }) {
                    Text("Previous")
                }
                Button(onClick = {
                    recipeViewModel.speak(steps[currentIndex])
                }) {
                    Text("Repeat")
                }
                Button(onClick = {
                    recipeViewModel.stopSpeaking()
                    currentIndex++
                }) {
                    Text("Next")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    recipeViewModel.stopSpeaking()
                    onBackToRecipes()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to recipes")
            }
        }
    }
}
