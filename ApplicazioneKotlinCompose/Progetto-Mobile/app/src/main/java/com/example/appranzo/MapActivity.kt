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
import com.example.appranzo.ui.screens.MapScreen
import com.example.appranzo.ui.screens.ThemeViewModel
import com.example.appranzo.ui.theme.APPranzoTheme
import org.koin.androidx.compose.koinViewModel

class MapActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lat = intent.getDoubleExtra("lat", Double.NaN).takeIf { !it.isNaN() }
        val lng = intent.getDoubleExtra("lng", Double.NaN).takeIf { !it.isNaN() }

        setContent {
            MapContainer(lat, lng)
        }
    }
}

@Composable
fun MapContainer(lat: Double?, lng: Double?) {
    val themeViewModel: ThemeViewModel = koinViewModel()
    val themeState by themeViewModel.state.collectAsStateWithLifecycle()

    val darkMode = when (themeState.theme) {
        Theme.Light  -> false
        Theme.Dark   -> true
        Theme.System -> isSystemInDarkTheme()
    }

    APPranzoTheme(darkTheme = darkMode) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MapScreen(lat, lng)
        }
    }
}
