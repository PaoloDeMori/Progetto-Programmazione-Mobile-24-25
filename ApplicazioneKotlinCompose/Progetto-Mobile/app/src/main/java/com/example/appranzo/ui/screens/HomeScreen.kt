package com.example.appranzo.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.appranzo.PlaceDetailActivity
import com.example.appranzo.R
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.data.models.Category
import com.example.appranzo.data.models.Place
import com.example.appranzo.ui.navigation.Routes
import com.example.appranzo.util.PermissionStatus
import com.example.appranzo.util.permissions.LocationService
import com.example.appranzo.util.rememberMultiplePermissions
import com.example.appranzo.viewmodel.PlacesViewModel
import kotlinx.coroutines.launch
import java.util.Locale

fun onClickPlace(place: Place, ctx: Context,position:Int) {
    val intent = Intent(ctx, PlaceDetailActivity::class.java)
    intent.putExtra("EXTRA_PRODUCT_ID", place.id)
    intent.putExtra("DISTANCE", place.distanceFromUser)
    intent.putExtra("WITHPOSITION", position)
    ctx.startActivity(intent)
}

@Composable
fun HomeScreen(navController: NavController,placesViewModel: PlacesViewModel) {
    val ctx = LocalContext.current
    val homePageState by placesViewModel.homePageState.collectAsStateWithLifecycle()
    var showLocationDisabledAlert by remember { mutableStateOf(false) }
    var showPermissionDeniedAlert by remember { mutableStateOf(false) }
    var showPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    val locationService = remember { LocationService(ctx) }
    val isLoading by locationService.isLoadingLocation.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current


    fun getCurrentLocation() = scope.launch {
        try {
            locationService.getCurrentLocation()?.let{placesViewModel.loadNearPlaces(it.latitude,it.longitude)}
        } catch (_: IllegalStateException) {
            showLocationDisabledAlert = true
        }
    }


    val locationPermissions = rememberMultiplePermissions(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    ) { statuses ->
        when {
            statuses.any { it.value == PermissionStatus.Granted } ->
                getCurrentLocation()
            statuses.all { it.value == PermissionStatus.PermanentlyDenied } ->
                showPermissionPermanentlyDenied = true
            else ->
                showPermissionDeniedAlert = true
        }
    }

    fun checkAndRequestPermissions() {
        if (locationPermissions.statuses.values.any { it.isGranted }) {
            getCurrentLocation()
        } else {
            locationPermissions.launchPermissionRequest()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                placesViewModel.loadTopRatedPlaces()
                checkAndRequestPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            SearchBar(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { navController.navigate(Routes.SEARCH) }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            CategoryRow(
                modifier = Modifier,
                listOf(
                    Category(1,"Cafè"),
                    Category(2,"Bar"),
                    Category(3,"Ristorante"),
                    Category(4,"Pizzeria"),
                    Category(5, "Pub")
                ),
                navController
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            MainTitle("Alta Valutazione", modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            HighlitedRestaurants(
                places             = homePageState.topRatedPlaces,
                favouritePlaces    = homePageState.favouritePlaces,
                onToggleFavourite  = { p -> placesViewModel.toggleFavourites(p) },
                onClickColumn     = { place, index -> onClickPlace(place, ctx, index+1) }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryTitle("Vicino a Me", modifier = Modifier.padding(horizontal = 16.dp))
                Icon(Icons.Filled.LocalDining, "ArrowIcon")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoading) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(100.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 10.dp,
                    )
                }
            } else {
                when {
                    showLocationDisabledAlert -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Attiva il GPS per trovare i ristoranti vicini.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { locationService.openLocationSettings() }) {
                                Text("Apri Impostazioni GPS")
                            }
                        }
                    }

                    showPermissionPermanentlyDenied -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Permessi Gps Negati permanentemente, riattivali dalle impostazioni per un'esperienza completa")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    showPermissionDeniedAlert -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Permessi GPS Negati, richiedili per vedere i ristoranti vicini a te")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { locationPermissions.launchPermissionRequest() }) {
                                Text("Richiedi permessi GPs")
                            }
                        }
                    }

                    else ->
                        if (homePageState.nearPlaces.isNotEmpty()) {
                            for (p in homePageState.nearPlaces) {
                                val isFavourite =
                                    homePageState.favouritePlaces.any { it.id == p.id }
                                PlaceWithDescription(
                                    place = p,
                                    modifier = Modifier,
                                    isFavourite = isFavourite,
                                    OnTOggleFavourite = { placesViewModel.toggleFavourites(p) },
                                    OnClickPlace = { pl -> onClickPlace(pl, ctx,0) }
                                )
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                        } else {
                            Text(
                                text = "Nessun ristorante trovato nelle vicinanze.",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,

    ) {
        Text(
            "Cerca",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
        )
    }
}

@Composable
fun CategoryRow(modifier: Modifier = Modifier, categories: List<Category>,navController: NavController) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items(categories) { c ->
            val imageName = when (c.name){
                "Pizzeria"->R.drawable.pizzeria
                "Pub"->R.drawable.pub
                "Ristorante"->R.drawable.ristorante
                "Cafè"->R.drawable.cafe
                "Bar"->R.drawable.bar
                else->R.drawable.restaurantplaceholder
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { navController.navigate("${Routes.SEARCH_RESULT}/${c.id}") }
            ) {
                AsyncImage(
                    model = imageName,
                    contentDescription = "${c.name} photo preview",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ristorante),
                    placeholder = painterResource(id = imageName)
                )
                Text(
                    c.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MainTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun HighlitedRestaurants(
    places: List<Place>,
    favouritePlaces: List<Place>,
    onToggleFavourite: (Place) -> Unit,
    onClickColumn: (Place, Int) -> Unit
) {
    LazyRow(
        modifier = Modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(places) { index, place ->
            val isFavourite = favouritePlaces.any { it.id == place.id }
            Box {
                Column(modifier = Modifier.clickable { onClickColumn(place,index) }) {
                    Card(modifier = Modifier.size(width = 280.dp, height = 180.dp)) {
                        AsyncImage(
                            model = "${RestApiClient.REST_API_ADDRESS}/places/photos/${place.photoUrl}",
                            contentDescription = place.description,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.restaurantplaceholder),
                            placeholder = painterResource(id = R.drawable.restaurantplaceholder)
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        place.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row {
                        repeat(place.rating.toInt()) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Rating icon")
                        }
                    }
                }
                IconButton(
                    onClick = { onToggleFavourite(place) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                ) {
                    if (isFavourite) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Rimosso dai preferiti",
                            tint = Color.Red,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Aggiunto ai preferiti",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecondaryTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun PlaceWithDescription(
    place: Place,
    modifier: Modifier = Modifier,
    isFavourite: Boolean,
    OnTOggleFavourite: ()->Unit,
    OnClickPlace: (Place) -> Unit
) {
    Card(modifier = modifier.fillMaxWidth().clickable { OnClickPlace(place) }) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box {
                AsyncImage(
                    model = "${RestApiClient.REST_API_ADDRESS}/places/photos/${place.photoUrl}",
                    contentDescription = place.description,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.restaurantplaceholder),
                    placeholder = painterResource(id = R.drawable.restaurantplaceholder)
                )
                IconButton(
                    onClick = { OnTOggleFavourite() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .size(30.dp)
                ) {
                    if (isFavourite) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Rimosso dai preferiti",
                            tint = Color.Red,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Aggiunto ai preferiti",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Text(place.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                val ratingValue = (place.rating).toFloat()
                val fullStars   = ratingValue.toInt()
                val hasHalfStar = (ratingValue - fullStars) >= 0.5f
                val emptyStars  = 5 - fullStars - if (hasHalfStar) 1 else 0

                Row {
                    repeat(fullStars) {
                        Icon(
                            imageVector   = Icons.Default.StarRate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (hasHalfStar) {
                        Icon(
                            imageVector   = Icons.AutoMirrored.Filled.StarHalf,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    repeat(emptyStars) {
                        Icon(
                            imageVector   = Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

            }
                Text(place.description ?: "Place", fontSize = 14.sp)
            }
        }
    }
}
