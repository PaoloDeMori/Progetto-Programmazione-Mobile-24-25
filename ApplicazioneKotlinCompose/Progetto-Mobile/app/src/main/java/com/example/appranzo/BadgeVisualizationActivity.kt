package com.example.appranzo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.communication.remote.loginDtos.UserDto
import com.example.appranzo.data.models.Theme

import com.example.appranzo.ui.screens.BadgeRoadScreen
import com.example.appranzo.ui.screens.ThemeViewModel
import com.example.appranzo.ui.theme.APPranzoTheme
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel


class BadgeVisualizationActivity: ComponentActivity() {
    private val restApiClient: RestApiClient by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = intent.getStringExtra("USER_ID")

        val errorText = "Impossible to load this account"
        val duration = Toast.LENGTH_SHORT

        if(user==null){
            val toast = Toast.makeText(this, errorText, duration)
            toast.show()
            finish()
            return
        }

        try{
            val userDecoded = Json.decodeFromString<UserDto>(user)
            setContent {
                ShowBadgesScreen(userDecoded,this)
            }
        }
        catch (e: Exception){
            val toast = Toast.makeText(this, errorText, duration)
            toast.show()
            finish()
            return
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BadgeTopAppBar(
    onCloseClicked: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = "Badge")
        },
        navigationIcon = {
            IconButton(onClick = onCloseClicked) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Chiudi"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ShowBadgesScreen(userDto: UserDto,activity:ComponentActivity) {
    val themeViewModel: ThemeViewModel = koinViewModel()

    val ctx = LocalContext.current


    val themeState by themeViewModel.state.collectAsStateWithLifecycle()

    val darkModeEnabled = when (themeState.theme) {
        Theme.Light  -> false
        Theme.Dark   -> true
        Theme.System -> isSystemInDarkTheme()
    }

    APPranzoTheme(darkTheme = darkModeEnabled) {

        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    BadgeTopAppBar(
                        onCloseClicked = {
                            activity.finish()
                        }
                    )
                }
            ) {
                BadgeRoadScreen(
                    userDto = userDto,
                )
            }
        }
    }
}