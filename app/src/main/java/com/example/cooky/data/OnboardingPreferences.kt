package com.example.cooky.data

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "cooky_prefs"
private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"

fun Context.onboardingPreferences(): SharedPreferences =
    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

fun SharedPreferences.hasCompletedOnboarding(): Boolean =
    getBoolean(KEY_ONBOARDING_COMPLETE, false)

fun SharedPreferences.setOnboardingComplete() {
    edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
}
