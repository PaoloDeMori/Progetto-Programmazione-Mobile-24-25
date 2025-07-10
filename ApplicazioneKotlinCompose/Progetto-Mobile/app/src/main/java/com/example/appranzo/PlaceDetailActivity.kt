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
import com.example.appranzo.ui.screens.RestaurantDetailContent
import com.example.appranzo.ui.screens.ThemeViewModel
import com.example.appranzo.ui.theme.APPranzoTheme
import org.koin.androidx.compose.koinViewModel

class PlaceDetailActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val receivedId = intent.getIntExtra("EXTRA_PRODUCT_ID", -1)
        val receivedDistance = intent.getDoubleExtra("DISTANCE", 0.0)
        val withPosition = intent.getIntExtra("WITHPOSITION", 0)
        setContent {
            PlaceDetail(receivedId,receivedDistance,withPosition)
        }
    }
}

@Composable
fun PlaceDetail(id:Int,distance:Double,position:Int) {
    val themeViewModel: ThemeViewModel = koinViewModel()
    val themeState by themeViewModel.state.collectAsStateWithLifecycle()
    val darkModeEnabled = when (themeState.theme) {
        Theme.Light -> false
        Theme.Dark -> true
        Theme.System -> isSystemInDarkTheme()
    }
    APPranzoTheme(darkTheme = darkModeEnabled) {

        Surface(color = MaterialTheme.colorScheme.background) {
            RestaurantDetailContent(id,distance,position, koinViewModel())
        }
    }
}