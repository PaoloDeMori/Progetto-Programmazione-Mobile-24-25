package com.example.appranzo.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appranzo.viewmodel.ProfileDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileDetailScreen(
    navController: NavController,
    viewModel: ProfileDetailViewModel = koinViewModel()
) {
    val user by viewModel.user.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettagli Profilo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val u = user) {
                null -> CircularProgressIndicator()
                else -> Column(
                    horizontalAlignment   = Alignment.CenterHorizontally,
                    verticalArrangement   = Arrangement.spacedBy(16.dp),
                    modifier              = Modifier.padding(24.dp)
                ) {
                    u.photoUrl?.let { url ->
                        AsyncImage(
                            model              = url,
                            contentDescription = "Foto profilo",
                            modifier           = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } ?: Icon(
                        Icons.Default.Person,
                        contentDescription = "Placeholder foto",
                        modifier           = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )

                    Text("Username: ${u.username}", style = MaterialTheme.typography.titleMedium)
                    Text("Email:    ${u.email}",    style = MaterialTheme.typography.bodyLarge)
                    Text("Punti:    ${u.points}",   style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
