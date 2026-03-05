package com.example.cooky.data

data class Recipe(
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val category: String? = null
)
