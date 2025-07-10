package com.example.appranzo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.data.models.Theme
import com.example.appranzo.data.repository.TokensRepository
import com.example.appranzo.ui.navigation.LoginNavGraph
import com.example.appranzo.ui.screens.ThemeViewModel
import com.example.appranzo.ui.theme.APPranzoTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class LoginActivity: ComponentActivity() {
    private val tokensRepository: TokensRepository by inject()
    private val restApiClient: RestApiClient by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginAndRegistration()
        }
    }
}

@Composable
fun LoginAndRegistration() {
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
            LoginNavGraph(navController = navController)
        }
    }
}