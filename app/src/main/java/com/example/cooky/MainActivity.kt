package com.example.cooky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cooky.ui.CookyNavHost
import com.example.cooky.ui.theme.CookyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookyTheme {
                CookyNavHost()
            }
        }
    }
}
