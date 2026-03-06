package com.example.cooky.data

object SampleRecipes {
    val list: List<Recipe> = listOf(
        Recipe(
            title = "Scrambled Eggs",
            ingredients = listOf(
                "2 large eggs",
                "1 tablespoon butter",
                "Salt and pepper to taste",
                "1 tablespoon milk (optional)"
            ),
            steps = listOf(
                "Crack the eggs into a bowl and whisk until the yolks and whites are combined.",
                "Add a pinch of salt and pepper, and milk if using. Mix well.",
                "Melt the butter in a non-stick pan over medium-low heat.",
                "Pour in the egg mixture. Let it set for a few seconds.",
                "Gently push the eggs from the edges toward the center with a spatula.",
                "Continue until the eggs are softly set but still slightly wet. Serve immediately."
            )
        ),
        Recipe(
            title = "Garlic Butter Pasta",
            ingredients = listOf(
                "8 oz spaghetti",
                "4 cloves garlic, minced",
                "4 tablespoons butter",
                "2 tablespoons olive oil",
                "Fresh parsley, chopped",
                "Parmesan cheese and salt to taste"
            ),
            steps = listOf(
                "Bring a large pot of salted water to a boil. Cook the spaghetti according to package directions until al dente. Reserve 1/2 cup pasta water, then drain.",
                "While the pasta cooks, melt the butter with the olive oil in a large skillet over medium heat.",
                "Add the minced garlic and cook for about 1 minute until fragrant. Do not let it brown.",
                "Add the drained pasta to the skillet and toss to coat. Add pasta water a little at a time if it seems dry.",
                "Turn off the heat. Stir in parsley and Parmesan. Season with salt and serve."
            )
        ),
        Recipe(
            title = "Avocado Toast",
            ingredients = listOf(
                "2 slices sourdough or whole grain bread",
                "1 ripe avocado",
                "Lemon juice",
                "Salt, red pepper flakes, and olive oil to taste"
            ),
            steps = listOf(
                "Toast the bread until golden and crisp.",
                "Halve the avocado, remove the pit, and scoop the flesh into a bowl.",
                "Mash the avocado with a fork. Add a squeeze of lemon juice and a pinch of salt.",
                "Spread the mashed avocado on the toast. Drizzle with olive oil and sprinkle with red pepper flakes. Serve right away."
            )
        )
    )
}
