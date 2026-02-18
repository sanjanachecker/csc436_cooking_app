package com.example.cooky.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
fun IngredientsPreparationScreen(
    recipeViewModel: RecipeViewModel,
    onFinished: () -> Unit
) {
    val recipe by recipeViewModel.recipe.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    recipe?.ingredients?.let { ingredients ->
        if (currentIndex >= ingredients.size) {
            onFinished()
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
                text = "Ingredient ${currentIndex + 1} of ${ingredients.size}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = ingredients[currentIndex],
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cooky would read this ingredient aloud",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { currentIndex++ }) {
                Text("Next Ingredient")
            }
        }
    }
}
