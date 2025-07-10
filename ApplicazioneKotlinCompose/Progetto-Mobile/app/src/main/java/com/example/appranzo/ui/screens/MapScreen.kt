package com.example.appranzo.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.preference.PreferenceManager
import com.example.appranzo.data.models.Place
import com.example.appranzo.util.PermissionStatus
import com.example.appranzo.util.permissions.LocationService
import com.example.appranzo.util.rememberMultiplePermissions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay



@Composable
fun MapScreen(lat: Double?, lng: Double?,   places: List<Place> = emptyList()  ) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val locationService = remember { LocationService(context) }
    var showLocationDisabledAlert by remember { mutableStateOf(false) }
    var showPermissionDeniedAlert by remember { mutableStateOf(false) }
    var showPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var showPermissionGained by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current


    fun handleLocationRequest() = scope.launch {
        try {
            locationService.getCurrentLocation()
            showPermissionGained = true
        } catch (e: IllegalStateException) {
            showLocationDisabledAlert= true
        } catch (e: SecurityException) {
            showPermissionPermanentlyDenied=true
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))
    }

    val locationPermissions = rememberMultiplePermissions(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    ) { statuses ->
        when {
            statuses.any { it.value == PermissionStatus.Granted } -> {showPermissionGained= true; handleLocationRequest()}
            statuses.all { it.value == PermissionStatus.PermanentlyDenied } -> showPermissionPermanentlyDenied = true
            else -> showPermissionDeniedAlert = true
        }
    }

    fun checkAndRequestPermissions() {
        if (locationPermissions.statuses.values.any { it.isGranted }) {
            handleLocationRequest()
        } else {
            locationPermissions.launchPermissionRequest()
        }
    }

    LaunchedEffect(Unit) {
            checkAndRequestPermissions()
    }

    Scaffold(
        floatingActionButton = {
            if (lat != null && lng != null) {
                FloatingActionButton(
                    onClick = { activity?.finish() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Chiudi mappa")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            if (!showLocationDisabledAlert) {
                if (showPermissionGained) {
                    OsmdroidMap(lat = lat, lng = lng, places = places)
                }
                if(showPermissionDeniedAlert){
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Serve permesso di localizzazione per utilizzare la mappa")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { locationPermissions.launchPermissionRequest() }) {
                            Text("Concedi permesso")
                        }
                    }
                }
                if(showPermissionPermanentlyDenied){
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Serve permesso di localizzazione per utilizzare la mappa, riattivalo dalle impostazione del tuo telefono")
                        }
                }
            }
            else{
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
        }
    }
}


@Composable
private fun OsmdroidMap(lat: Double?, lng: Double?,  places: List<Place>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(4500)
        visible = true
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    DisposableEffect(mapView) {
        val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            enableFollowLocation()
            isDrawAccuracyEnabled = true
        }
        mapView.overlays.add(locOverlay)

        when {
            lat != null && lng != null -> {
                val point = GeoPoint(lat, lng)
                Marker(mapView).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }.also {
                    mapView.overlays.add(it)
                    mapView.controller.setCenter(point)
                    mapView.controller.setZoom(16.0)
                }
            }

            places.isNotEmpty() -> {
                places.forEach { place ->
                    val pt = GeoPoint(place.latitude, place.longitude)
                   val marker = Marker(mapView).apply {
                        position = pt
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = place.name
                       setOnMarkerClickListener { _, _ ->
                           onClickPlace(place, context, 0)
                           true
                       }
                    }.also { mapView.overlays.add(it) }
                }
                val first = places.first()
                mapView.controller.setCenter(GeoPoint(first.latitude, first.longitude))
                mapView.controller.setZoom(14.0)
            }


            else -> {
                val locOverlay =
                    MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
                        enableMyLocation(); enableFollowLocation(); isDrawAccuracyEnabled = true
                    }
                mapView.overlays.add(locOverlay)
            }
        }
        onDispose {
            mapView.overlays.clear()
        }
    }


    DisposableEffect(mapView, lat, lng) {
        val marker = if (lat != null && lng != null) {
            Marker(mapView).apply {
                val geoPoint = GeoPoint(lat, lng)
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    Toast.makeText(context, "$lat, $lng", Toast.LENGTH_SHORT).show()
                    true
                }
            }.also {
                mapView.overlays.add(it)
            }
        } else null

        onDispose {
            marker?.let { mapView.overlays.remove(it) }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
                .alpha(if (visible) 1f else 0f)
        )

        if (!visible) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center

            ) {
                CircularProgressIndicator()
            }
        }
    }
}




