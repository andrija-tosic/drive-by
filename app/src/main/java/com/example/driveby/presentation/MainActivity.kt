package com.example.driveby.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.driveby.navigation.NavGraph
import com.example.driveby.navigation.Screen
import com.example.driveby.presentation.home.BottomBar
import com.example.driveby.ui.theme.DriveByTheme
import dagger.hilt.android.AndroidEntryPoint
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@AndroidEntryPoint
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private val viewModel by viewModels<MainViewModel>()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberAnimatedNavController()
            DriveByTheme {
                Scaffold(
                    bottomBar = { BottomBar(navController = navController) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding))
                    {
                        NavGraph(
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AuthState() {
        val isUserSignedOut = viewModel.getAuthState().collectAsState().value
        if (isUserSignedOut) {
            NavigateToSignInScreen()
        } else {
//            if (viewModel.isEmailVerified) {
            NavigateToProfileScreen()
//            } else {
//                NavigateToVerifyEmailScreen()
//            }
        }
    }

    @Composable
    private fun NavigateToSignInScreen() = navController.navigate(Screen.SignInScreen.route) {
        popUpTo(navController.graph.id) {
            inclusive = true
        }
    }

    @Composable
    private fun NavigateToProfileScreen() = navController.navigate(Screen.ProfileScreen.route) {
        popUpTo(navController.graph.id) {
            inclusive = true
        }
    }

    @Composable
    private fun NavigateToVerifyEmailScreen() =
        navController.navigate(Screen.VerifyEmailScreen.route) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
}