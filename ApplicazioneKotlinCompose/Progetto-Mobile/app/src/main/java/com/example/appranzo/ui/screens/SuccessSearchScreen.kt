package com.example.appranzo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appranzo.viewmodel.SuccessSearchViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.items


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun SuccessSearchScreen(
    categoryId: Int,
    viewModel: SuccessSearchViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val places by viewModel.places.collectAsState()
    val ctx = LocalContext.current

    val favouritePlaces by viewModel.favouritePlaces.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.load(categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ecco i risultati della ricerca") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(places) { place ->
                PlaceWithDescription(place = place, modifier = Modifier,favouritePlaces.any { it.id == place.id },{viewModel.toggleFavourites(place)}) { p ->
                    onClickPlace(place,ctx, -1)
                }
            }
        }
    }
}
