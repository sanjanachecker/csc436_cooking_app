package com.example.cooky.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class OnboardingPage(
    val title: String,
    val body: String
)

private val pages = listOf(
    OnboardingPage(
        title = "Welcome to Cooky",
        body = "Your hands-free cooking assistant. Cooky reads recipes aloud and listens for your voice so you can keep your hands in the kitchen."
    ),
    OnboardingPage(
        title = "How it works",
        body = "Pick a recipe or paste your own. Cooky will read each ingredient and step aloud. Say \"Next\" to advance, \"Previous\" to go back, or \"Repeat\" to hear the current step again."
    ),
    OnboardingPage(
        title = "Get started",
        body = "Try a sample recipe or paste a recipe URL or text. You can use voice commands on the cooking and ingredients screens."
    )
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pageIndex by remember { mutableIntStateOf(0) }
    val page = pages[pageIndex]
    val isLastPage = pageIndex == pages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally { it }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = page.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onComplete() }) {
                Text("Skip")
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pageIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            if (isLastPage) {
                Button(
                    onClick = { onComplete() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get started", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Button(
                    onClick = { pageIndex++ },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
