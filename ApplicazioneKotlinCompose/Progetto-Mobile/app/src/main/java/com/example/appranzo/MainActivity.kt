package com.example.appranzo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.appranzo.data.models.Theme
import com.example.appranzo.ui.navigation.AppNavGraph
import com.example.appranzo.ui.screens.ThemeViewModel
import com.example.appranzo.ui.theme.APPranzoTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val themeViewModel: ThemeViewModel = koinViewModel()
    val themeState by themeViewModel.state.collectAsStateWithLifecycle()
    val darkModeEnabled = when (themeState.theme) {
        Theme.Light  -> false
        Theme.Dark   -> true
        Theme.System -> isSystemInDarkTheme()
    }

    APPranzoTheme(darkTheme = darkModeEnabled) {

        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            AppNavGraph(navController = navController)
        }
    }
}