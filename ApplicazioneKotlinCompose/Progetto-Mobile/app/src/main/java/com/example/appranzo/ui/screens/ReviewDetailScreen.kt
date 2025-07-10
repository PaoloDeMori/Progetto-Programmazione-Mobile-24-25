
package com.example.appranzo.ui.screens

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appranzo.viewmodel.ReviewDetailViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.appranzo.communication.remote.RestApiClient

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailScreen(
    restaurantId: Int,
    reviewId: Int,
    viewModel: ReviewDetailViewModel = koinViewModel()
) {
    val activity = LocalContext.current as? ComponentActivity

    LaunchedEffect(restaurantId, reviewId) {
            viewModel.loadReviewDetail(placeId = restaurantId, reviewId = reviewId)
    }
    val ui by viewModel.review.collectAsState()
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettaglio recensione") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        ui?.let { r ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Ristorante: ${r.placeName}", style = MaterialTheme.typography.titleMedium)
                Text("Autore: ${r.username}",      style = MaterialTheme.typography.bodyMedium)
                Text("Data: ${r.creationDate}",    style = MaterialTheme.typography.bodySmall)

                Text("Valutazione complessiva", style = MaterialTheme.typography.titleSmall)
                Row {
                    repeat(r.rating) {
                        Icon(Icons.Default.StarRate, contentDescription = null)
                    }
                    repeat(5 - r.rating) {
                        Icon(Icons.Default.StarBorder, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text("Prezzo", style = MaterialTheme.typography.titleSmall)
                Row {
                    repeat(r.priceLevel) {
                        Icon(Icons.Default.StarRate, contentDescription = null)
                    }
                    repeat(5 - r.priceLevel) {
                        Icon(Icons.Default.StarBorder, contentDescription = null)
                    }
                }

                Text("Ambiente", style = MaterialTheme.typography.titleSmall)
                Row {
                    repeat(r.ambienceRating) {
                        Icon(Icons.Default.StarRate, contentDescription = null)
                    }
                    repeat(5 - r.ambienceRating) {
                        Icon(Icons.Default.StarBorder, contentDescription = null)
                    }
                }

                Text("Qualità ingredienti", style = MaterialTheme.typography.titleSmall)
                Row {
                    repeat(r.ingredientQuality) {
                        Icon(Icons.Default.StarRate, contentDescription = null)
                    }
                    repeat(5 - r.ingredientQuality) {
                        Icon(Icons.Default.StarBorder, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(r.comment, style = MaterialTheme.typography.bodyLarge)

                r.photos.takeIf { it.isNotEmpty() }?.let { photos ->
                    Spacer(Modifier.height(16.dp))
                    Text("Foto:", style = MaterialTheme.typography.titleSmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        photos.forEach { url ->
                            AsyncImage(
                                model = "${RestApiClient.REST_API_ADDRESS}/places/photos/$url",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.LightGray)
                                    .clickable { selectedPhotoUrl = url },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
        selectedPhotoUrl?.let { url ->
            Dialog(onDismissRequest = { selectedPhotoUrl = null }) {
                AsyncImage(
                    model = "${RestApiClient.REST_API_ADDRESS}/places/photos/$url",
                    contentDescription = "Foto recensione ingrandita",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { selectedPhotoUrl = null }
                )
            }
        }
    }
}
