
package com.example.appranzo.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.appranzo.MapActivity
import com.example.appranzo.R
import com.example.appranzo.ReviewActivity
import com.example.appranzo.ReviewDetailActivity
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.viewmodel.PlaceDetailViewModel
import com.example.appranzo.viewmodel.RestaurantDetailActualState
import org.koin.androidx.compose.koinViewModel

@SuppressLint("DefaultLocale")
@Composable
fun RestaurantDetailContent(
    restaurantId: Int,
    distanceFromUser:Double,
    position:Int,
    viewModel: PlaceDetailViewModel = koinViewModel()
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val detailState      by viewModel.state.collectAsStateWithLifecycle()
    val reviews          by viewModel.reviews.collectAsStateWithLifecycle()
    val isReviewLoading  by viewModel.isReviewsLoading.collectAsStateWithLifecycle()

    LaunchedEffect(restaurantId) {
        viewModel.loadRestaurantById(restaurantId)
        viewModel.loadReviews(restaurantId)
    }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadRestaurantById(restaurantId)
                viewModel.loadReviews(restaurantId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    when (val actualState = detailState) {
        is RestaurantDetailActualState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is RestaurantDetailActualState.Success -> {
            val restaurant = actualState.restaurant

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Gray)
                    ) {
                        AsyncImage(
                            model = "${RestApiClient.REST_API_ADDRESS}/places/photos/${restaurant.photoUrl}",
                            contentDescription = restaurant.description,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds,
                            error = painterResource(id = R.drawable.restaurantplaceholder),
                            placeholder = painterResource(id = R.drawable.restaurantplaceholder)
                        )
                        IconButton(
                            onClick = { (context as? ComponentActivity)?.finish() },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp, 56.dp, 16.dp, 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Indietro",
                                tint = Color.White
                            )
                        }
                    }
                }

                item {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                    ) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        val ctx = LocalContext.current
                        AddressLink(
                            address   = restaurant.address.orEmpty(),
                            latitude  = restaurant.latitude,
                            longitude = restaurant.longitude,
                            context   = ctx
                        )
                        Spacer(Modifier.height(4.dp))

                        if(position==0) {
                            Text(
                                text = "${String.format("%.1f", distanceFromUser)}km",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        else{
                            if(position!=-1) {
                                val phrase = "In classifica"
                                val emoji1 = "\uD83E\uDD47"
                                val emoji2 = "\uD83E\uDD48"
                                val emoji3 = "\uD83E\uDD47"
                                Text(
                                    text = when (position) {
                                        1 -> "$phrase $emoji1"
                                        2 -> "$phrase $emoji2"
                                        3 -> "$phrase $emoji3"
                                        else -> {
                                            "$phrase #$position"
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                    ) {
                        Text("Valutazione media", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%.1f", restaurant.rating),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            val full = restaurant.rating.toInt()
                            val half = (restaurant.rating - full) >= 0.5
                            (1..5).forEach { i ->
                                val icon = when {
                                    i <= full       -> Icons.Default.StarRate
                                    i == full + 1 && half -> Icons.AutoMirrored.Filled.StarHalf
                                    else            -> Icons.Default.StarBorder
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Distribuzione voti", style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                    val counts = (1..5).associateWith { star ->
                        reviews.count { it.rating.toInt() == star }
                    }
                    val maxC = counts.values.maxOrNull() ?: 1
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        (5 downTo 1).forEach { star ->
                            RatingBarRow(
                                star = star,
                                count = counts[star] ?: 0,
                                maxCount = maxC,
                                barHeight = 12.dp,
                                barColor = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Recensioni", style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }

                if (isReviewLoading) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        }
                    }
                } else {
                    if (reviews.isEmpty()) {
                        item {
                            Text(
                                "Nessuna recensione disponibile.",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        items(reviews) { review ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                ReviewItem(
                                    review = Review(
                                        author  = review.username,
                                        date    = review.creationDate,
                                        rating  = review.rating.toInt(),
                                        content = review.comment?:""
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Vedi in dettaglio →",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .clickable {
                                            Intent(context, ReviewDetailActivity::class.java).also { intent ->
                                                intent.putExtra(ReviewDetailActivity.EXTRA_RESTAURANT_ID, restaurantId)
                                                intent.putExtra(ReviewDetailActivity.EXTRA_REVIEW_ID,     review.id)
                                                context.startActivity(intent)
                                            }
                                        }

                                )
                            }
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                Intent(context, ReviewActivity::class.java).also {
                                    it.putExtra("restaurantId", restaurantId)
                                    context.startActivity(it)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Lascia una recensione")
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }

        is RestaurantDetailActualState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Impossible to load this restaurant")
            }
        }
    }
}

@Composable
private fun RatingBarRow(
    star: Int,
    count: Int,
    maxCount: Int,
    barHeight: Dp,
    barColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$star", modifier = Modifier.width(24.dp))
        Icon(Icons.Default.StarRate, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(barHeight)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (maxCount > 0) count / maxCount.toFloat() else 0f)
                    .background(barColor, CircleShape)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("($count)", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(review.author, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.width(8.dp))
            Text(review.date, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Spacer(Modifier.height(4.dp))
        Row {
            repeat(review.rating) {
                Icon(Icons.Default.StarRate, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            repeat(5 - review.rating) {
                Icon(Icons.Default.StarBorder, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(review.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
    }
}

data class Review(val author: String, val date: String, val rating: Int, val content: String)


@Composable
fun AddressLink(
    address: String,
    latitude: Double,
    longitude: Double,
    context: Context
) {
    var showDialog by remember { mutableStateOf(false) }

    Text(
        text = address,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        ),
        modifier = Modifier.clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Apri indirizzo") },
            text = { Text("Scegli dove aprire la mappa:") },
            confirmButton = {
                TextButton(onClick = {
                    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(address)})")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    showDialog = false
                }) {
                    Text("Google Maps")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    Intent(context, MapActivity::class.java).also {
                        it.putExtra("lat", latitude)
                        it.putExtra("lng", longitude)
                        context.startActivity(it)
                    }
                    showDialog = false
                }) {
                    Text("Continua con APPranzo")
                }
            }
        )
    }
}

