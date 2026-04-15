package com.example.langfire_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.langfire_app.presentation.navigation.LangFireNavGraph
import com.example.langfire_app.presentation.ui.theme.LangFireappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LangFireappTheme {
                val navController = rememberNavController()
                LangFireNavGraph(navController = navController)
            }
        }
    }
}