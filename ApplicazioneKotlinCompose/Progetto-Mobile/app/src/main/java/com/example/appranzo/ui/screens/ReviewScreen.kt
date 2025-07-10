package com.example.appranzo.ui.screens

import android.app.Activity
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appranzo.viewmodel.ReviewViewModel
import com.example.appranzo.viewmodel.SubmissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    restaurantId: Int,
    viewModel: ReviewViewModel = koinViewModel()
) {
    val priceLevel by viewModel.priceLevel.collectAsStateWithLifecycle()
    val ambienceRating by viewModel.ambienceRating.collectAsStateWithLifecycle()
    val ingredientQuality by viewModel.ingredientQuality.collectAsStateWithLifecycle()

    val reviewText by viewModel.content.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                viewModel.addPhoto(bytes)
            }
        }
    }

    if (submitState is SubmissionState.Success) {
        LaunchedEffect(Unit) {
            viewModel.resetState()
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lascia una recensione") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Prezzo", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= priceLevel) Icons.Default.StarRate else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { viewModel.onPriceLevelChange(star) }
                    )
                }
            }

            Text("Ambiente", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= ambienceRating) Icons.Default.StarRate else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { viewModel.onAmbienceRatingChange(star) }
                    )
                }
            }

            Text("Qualità ingredienti", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= ingredientQuality) Icons.Default.StarRate else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { viewModel.onIngredientQualityChange(star) }
                    )
                }
            }

            OutlinedTextField(
                value = reviewText,
                onValueChange = viewModel::onContentChange,
                label = { Text("Recensione") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            Text("Foto", style = MaterialTheme.typography.titleMedium)
            if (photos.isEmpty()) {
                Text("Nessuna foto selezionata.")
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    photos.forEachIndexed { index, (_, byteArray) ->
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto $index",
                            modifier = Modifier
                                .size(64.dp)
                                .clickable { viewModel.removePhoto(index) }
                        )
                    }
                }
            }
            Button(onClick = { galleryLauncher.launch("image/*") }) {
                Text("Aggiungi immagini")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitReview(restaurantId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = submitState !is SubmissionState.Loading
            ) {
                if (submitState is SubmissionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Invia recensione")
                }
            }
        }
    }
}
