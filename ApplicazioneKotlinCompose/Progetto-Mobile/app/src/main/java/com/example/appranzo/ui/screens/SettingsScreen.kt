package com.example.appranzo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.appranzo.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ListItem(headlineContent = { Text("Aspetto / Tema") }, leadingContent = {
                    Icon(Icons.Default.Palette, contentDescription = null)
                }, trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }, modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Routes.SETTINGS_THEME)
                    })
            HorizontalDivider()
        }
    }
}
