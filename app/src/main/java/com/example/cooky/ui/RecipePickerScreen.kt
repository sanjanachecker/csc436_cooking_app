package com.example.cooky.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.cooky.data.Recipe
import com.example.cooky.data.SampleRecipes

@Composable
fun RecipePickerScreen(
    onlineRecipes: List<Recipe>,
    totalOnlineCount: Int,
    isLoadingOnline: Boolean,
    categories: List<String>,
    selectedLetter: String?,
    selectedCategory: String?,
    onLetterFilter: (String?) -> Unit,
    onCategoryFilter: (String?) -> Unit,
    onSelectRecipe: (Recipe) -> Unit,
    onPasteOwn: () -> Unit,
    onHelp: () -> Unit,
    onRetryOnline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 56.dp)
        ) {
            Text(
                text = "Choose a recipe",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            // Letter filter
            Text(
                text = "Starts with",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedLetter == null,
                    onClick = { onLetterFilter(null) },
                    label = { Text("All", style = MaterialTheme.typography.labelLarge) },
                    shape = RoundedCornerShape(20.dp)
                )
                ('a'..'z').forEach { letter ->
                    val s = letter.toString()
                    FilterChip(
                        selected = selectedLetter == s,
                        onClick = { onLetterFilter(s) },
                        label = { Text(s.uppercase(), style = MaterialTheme.typography.labelLarge) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Category filter
            if (categories.isNotEmpty()) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategoryFilter(null) },
                        label = { Text("All", style = MaterialTheme.typography.labelLarge) },
                        shape = RoundedCornerShape(20.dp)
                    )
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategoryFilter(category) },
                            label = { Text(category, style = MaterialTheme.typography.labelLarge) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // --- Online recipes ---
            Text(
                text = "Online recipes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp)
            )
            when {
                isLoadingOnline && totalOnlineCount == 0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                totalOnlineCount == 0 -> {
                    Text(
                        text = "Couldn't load recipes. Check connection and tap Retry.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(onClick = onRetryOnline) {
                        Text("Retry")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                onlineRecipes.isEmpty() -> {
                    Text(
                        text = "No recipes match the selected filters.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        onlineRecipes.forEachIndexed { index, recipe ->
                            RecipeRow(
                                recipe = recipe,
                                onClick = { onSelectRecipe(recipe) }
                            )
                            if (index < onlineRecipes.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            // --- Sample recipes ---
            Text(
                text = "Sample recipes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                SampleRecipes.list.forEachIndexed { index, recipe ->
                    RecipeRow(
                        recipe = recipe,
                        onClick = { onSelectRecipe(recipe) }
                    )
                    if (index < SampleRecipes.list.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            FilledTonalButton(
                onClick = onPasteOwn,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Paste your own recipe", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(onClick = onHelp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun RecipeRow(
    recipe: Recipe,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2
            )
        },
        supportingContent = if (recipe.ingredients.isNotEmpty()) {
            {
                Text(
                    text = "${recipe.ingredients.size} ingredients · ${recipe.steps.size} steps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}
