package com.example.cooky.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cooky.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeImportScreen(
    recipeViewModel: RecipeViewModel,
    onRecipeAnalyzed: () -> Unit,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            FilledTonalButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back", style = MaterialTheme.typography.labelLarge)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Paste your recipe",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("URL or recipe text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Button(
                onClick = {
                    recipeViewModel.parseAndSetRecipe(text)
                    onRecipeAnalyzed()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Analyze Recipe", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
