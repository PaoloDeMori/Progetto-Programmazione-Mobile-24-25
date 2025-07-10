package com.example.appranzo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.appranzo.R
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.viewmodel.PlacesViewModel


@Composable
fun FavoritesScreen(
    placesViewModel: PlacesViewModel
) {
    val homePageState by placesViewModel.homePageState.collectAsStateWithLifecycle()
    val favorites = homePageState.favouritePlaces
    val ctx = LocalContext.current

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (favorites.isEmpty()) {
                Text(
                    "Nessun ristorante nei preferiti",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favorites) { place ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onClickPlace(place,ctx,-1) }
                        ) {
                            Box(Modifier.fillMaxWidth()) {
                                IconButton(
                                    onClick = { placesViewModel.toggleFavourites(place) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Rimuovi",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Row(
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = "${RestApiClient.REST_API_ADDRESS}/places/photos/${place.photoUrl}",                                        contentDescription = place.description,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(id = R.drawable.restaurantplaceholder),
                                        placeholder = painterResource(id = R.drawable.restaurantplaceholder)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = place.name.replaceFirstChar { it.uppercaseChar() },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontSize = 16.sp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Row {
                                            repeat(place.rating.toInt()) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
