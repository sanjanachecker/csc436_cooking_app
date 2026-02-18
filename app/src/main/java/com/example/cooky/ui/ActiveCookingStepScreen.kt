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
    onRecipeComplete: () -> Unit
) {
    val recipe by recipeViewModel.recipe.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    recipe?.steps?.let { steps ->
        if (currentIndex >= steps.size) {
            onRecipeComplete()
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = steps[currentIndex],
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cooky is waiting for the user to say 'Next' (simulated)",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { if (currentIndex > 0) currentIndex-- }) {
                    Text("Previous")
                }
                Button(onClick = { /* Functionality to repeat is implicit */ }) {
                    Text("Repeat")
                }
                Button(onClick = { currentIndex++ }) {
                    Text("Next")
                }
            }
        }
    }
}
