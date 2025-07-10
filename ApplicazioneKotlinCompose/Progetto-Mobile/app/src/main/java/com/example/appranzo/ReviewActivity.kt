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
import com.example.appranzo.data.models.Theme
import com.example.appranzo.ui.screens.ReviewScreen
import com.example.appranzo.ui.screens.ThemeViewModel
import com.example.appranzo.ui.theme.APPranzoTheme
import org.koin.androidx.compose.koinViewModel

class ReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val restaurantId = intent.getIntExtra("restaurantId", -1)
        if (restaurantId == -1) {
            finish()
            return
        }

        setContent {
            ReviewScreenContent(restaurantId)
        }
    }
}

@Composable
fun ReviewScreenContent(restaurantId: Int) {
    val themeViewModel: ThemeViewModel = koinViewModel()
    val themeState by themeViewModel.state.collectAsStateWithLifecycle()
    val darkTheme = when (themeState.theme) {
        Theme.Light -> false
        Theme.Dark -> true
        Theme.System -> isSystemInDarkTheme()
    }



    APPranzoTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ReviewScreen(restaurantId = restaurantId)
        }
    }
}
