package com.example.driveby.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import com.example.driveby.presentation.home.HomeScreen
import com.example.driveby.presentation.sign_in.SignInScreen
import com.example.driveby.presentation.sign_in.profile.ProfileScreen
import com.example.driveby.presentation.sign_up.SignUpScreen
import com.example.driveby.presentation.verify_email.VerifyEmailScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

@Composable
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
fun NavGraph(
    navController: NavHostController
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.SignInScreen.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable(
            route = Screen.SignInScreen.route
        ) {
            SignInScreen(
                navigateToForgotPasswordScreen = {
                    navController.navigate(Screen.ForgotPasswordScreen.route)
                },
                navigateToSignUpScreen = {
                    navController.navigate(Screen.SignUpScreen.route)
                },
                navigateToHomeScreen = {
                    navController.navigate(Screen.HomeScreen.route)
                }
            )
        }
        composable(
            route = Screen.SignUpScreen.route
        ) {
            SignUpScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToHomeScreen = {
                    navController.navigate(Screen.HomeScreen.route)
                }
            )
        }
        composable(
            route = Screen.VerifyEmailScreen.route
        ) {
            VerifyEmailScreen(
                navigateToProfileScreen = {
                    navController.navigate(Screen.ProfileScreen.route)
                }
            )
        }
        composable(
            route = Screen.ProfileScreen.route
        ) {
            ProfileScreen(
                navigateToSignInScreen = {
                    navController.navigate(Screen.SignInScreen.route)
                }
            )
        }
        composable(
            route = Screen.HomeScreen.route
        ) {
            HomeScreen()
        }
    }
}
