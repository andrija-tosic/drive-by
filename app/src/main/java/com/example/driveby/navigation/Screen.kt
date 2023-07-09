package com.example.driveby.navigation

sealed class Screen(val route: String) {
    object SignInScreen: Screen("Sign in")
    object ForgotPasswordScreen: Screen("Forgot password")
    object SignUpScreen: Screen("Sign up")
    object VerifyEmailScreen: Screen("Verify email")
    object ProfileScreen: Screen("Profile")
    object HomeScreen: Screen("Home")
    object LeaderboardScreen: Screen("Leaderboard")
}