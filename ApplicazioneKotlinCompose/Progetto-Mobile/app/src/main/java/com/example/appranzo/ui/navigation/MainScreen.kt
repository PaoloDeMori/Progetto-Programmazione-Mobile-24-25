package com.example.appranzo.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.communication.remote.loginDtos.UserDto
import com.example.appranzo.ui.screens.*
import com.example.appranzo.viewmodel.BadgeRoadViewModel
import com.example.appranzo.viewmodel.FriendsViewModel
import com.example.appranzo.viewmodel.PlacesViewModel
import com.example.appranzo.viewmodel.ProfileDetailViewModel

import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val tabNav = rememberNavController()
    val restApiClient: RestApiClient = koinInject()

    val placesViewModel: PlacesViewModel = koinViewModel()

    val badgeRoadViewModel: BadgeRoadViewModel = koinViewModel()
    val badgeUiState by badgeRoadViewModel.uiState.collectAsState()


    val friendsViewModel: FriendsViewModel = koinViewModel()
    val pendingRequests by friendsViewModel.pendingRequests.collectAsState(initial = emptyMap())
    val pendingCount = pendingRequests.size

    val profileViewModel: ProfileDetailViewModel = koinViewModel()
    val homeState by placesViewModel.homePageState.collectAsState()

    val user by profileViewModel.user.collectAsState()

    LaunchedEffect(user) {
        user?.let {
            badgeRoadViewModel.loadBadgeData(it)
        }
    }

    Scaffold(
        topBar = {
            val backStack by tabNav.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            val screenTitle = when (currentRoute) {
                Routes.TAB_HOME      -> "APPranzo"
                Routes.TAB_FAVORITES -> "Preferiti"
                Routes.TAB_MAP       -> "Mappa"
                Routes.TAB_FRIENDS   -> "Amici"
                Routes.TAB_BADGES    -> "Badge"
                else -> "APPranzo"
            }

            TopAppBar(
                title = { Text(screenTitle) },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profilo")
                    }
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Impostazioni")
                    }
                }
            )
        },
        bottomBar = {
            val backStack by tabNav.currentBackStackEntryAsState()
            val current = backStack?.destination?.route
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = current == screen.route,
                        onClick = {
                            if (screen.route == Routes.TAB_BADGES) {
                                badgeRoadViewModel.onBadgesScreenViewed()
                            }
                            profileViewModel.refresh()
                            tabNav.navigate(screen.route) {
                                popUpTo(tabNav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon = {
                            when (screen.route) {
                                Routes.TAB_BADGES -> {
                                    BadgedBox(
                                        badge = {
                                            if (badgeUiState.hasNewBadges) {
                                                Badge()
                                            }
                                        }
                                    ) {
                                        Icon(screen.icon, contentDescription = screen.label)
                                    }
                                }
                                Routes.TAB_FRIENDS -> {
                                    BadgedBox(
                                        badge = {
                                            if (pendingCount > 0) {
                                                Badge {
                                                    Text(
                                                        text = pendingCount.toString(),
                                                        color = MaterialTheme.colorScheme.background,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(screen.icon, contentDescription = screen.label)
                                    }
                                }
                                else -> {
                                    Icon(screen.icon, contentDescription = screen.label)
                                }
                            }
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = tabNav,
            startDestination = Routes.TAB_HOME,
            modifier         = Modifier.padding(innerPadding)
        ) {


            composable(Routes.TAB_HOME)      { HomeScreen(navController,placesViewModel)}
            composable(Routes.TAB_FAVORITES) { FavoritesScreen(placesViewModel) }
            composable(Routes.TAB_MAP)       { MapScreen(null,null,  places = homeState.nearPlaces) }
            composable(Routes.TAB_FRIENDS)   { FriendsScreen(friendsViewModel) }
            composable(Routes.TAB_BADGES)    { BadgeRoadScreen(user?:UserDto(0,"Unknown","Unknown",0,"")) }
        }
    }
}
