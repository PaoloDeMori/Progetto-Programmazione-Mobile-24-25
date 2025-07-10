package com.example.appranzo.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavScreen(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavScreen(Routes.TAB_HOME,      Icons.Filled.Home,     "Home"),
    BottomNavScreen(Routes.TAB_FAVORITES, Icons.Filled.Favorite, "Preferiti"),
    BottomNavScreen(Routes.TAB_MAP,       Icons.Filled.Map,      "Mappa"),
    BottomNavScreen(Routes.TAB_FRIENDS,   Icons.Filled.Person,   "Amici"),
    BottomNavScreen(Routes.TAB_BADGES,    Icons.Filled.Star,     "Badge"),
)
