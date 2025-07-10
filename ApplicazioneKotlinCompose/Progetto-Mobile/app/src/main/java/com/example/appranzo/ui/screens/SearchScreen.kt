package com.example.appranzo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appranzo.ui.navigation.Routes
import com.example.appranzo.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = koinViewModel()
) {
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current

    var query by remember { mutableStateOf("") }
    val categories by viewModel.categories.collectAsState()
    val results by viewModel.results.collectAsState()
    val isNavigationInProgress by viewModel.isNavigationInProgress.collectAsState()

    Scaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.setQuery(it)
                    },
                    placeholder = { Text("Cerca ristorante…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
                IconButton(onClick = {
                    if(!isNavigationInProgress){
                    navController.popBackStack()
                        viewModel.startNavigationLock()
                    } },
                    enabled = !isNavigationInProgress) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi ricerca")
                }
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
        ) {
            if (query.isBlank()) {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        ListItem(
                            headlineContent = { Text(category.name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    focusManager.clearFocus()
                                    navController.navigate("${Routes.SEARCH_RESULT}/${category.id}")
                                }
                        )
                        HorizontalDivider()
                    }
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { place ->
                        ListItem(
                            headlineContent = { Text(place.name) },
                            supportingContent = { Text(place.description ?: "") },
                            modifier = Modifier.clickable {
                                focusManager.clearFocus()
                                onClickPlace(place, ctx,-1)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

