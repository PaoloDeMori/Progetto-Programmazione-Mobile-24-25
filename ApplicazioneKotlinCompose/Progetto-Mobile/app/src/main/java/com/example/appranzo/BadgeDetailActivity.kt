package com.example.appranzo

import androidx.compose.material.icons.filled.StarBorder
import com.example.appranzo.data.models.Badge
import com.example.appranzo.ui.screens.BadgeScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.data.models.Theme
import com.example.appranzo.ui.screens.ThemeViewModel
import com.example.appranzo.ui.theme.APPranzoTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class BadgeDetailActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val badgeDto = intent.getStringExtra("BADGE_ID")

        var badge = Badge(badgeDto?:"Unknown",Icons.Default.StarBorder,0,"")
        for(b in Badge.roadmapBadge){
            if(b.nome == badgeDto){
               badge = b
            }
        }

        setContent {
           ShowBadgeScreen(badge,this)
        }
    }
}

@Composable
fun ShowBadgeScreen(badge: Badge,activity:ComponentActivity) {
    val themeViewModel: ThemeViewModel = koinViewModel()


    val themeState by themeViewModel.state.collectAsStateWithLifecycle()

    val darkModeEnabled = when (themeState.theme) {
        Theme.Light  -> false
        Theme.Dark   -> true
        Theme.System -> isSystemInDarkTheme()
    }

    APPranzoTheme(darkTheme = darkModeEnabled) {

        Surface(color = MaterialTheme.colorScheme.background) {
                BadgeScreen(
                    badge,activity
                )
            }
        }
    }