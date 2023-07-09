package com.example.driveby

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.driveby.navigation.Screen

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Leaderboard : BottomBarScreen(
        route = Screen.LeaderboardScreen.route,
        title = Screen.LeaderboardScreen.route,
        icon = Icons.Outlined.Leaderboard
    )

    object Home : BottomBarScreen(
        route = Screen.HomeScreen.route,
        title = Screen.HomeScreen.route,
        icon = Icons.Outlined.Home
    )

    object Profile : BottomBarScreen(
        route = Screen.ProfileScreen.route,
        title = Screen.ProfileScreen.route,
        icon = Icons.Outlined.Person
    )
}