package com.example.driveby.presentation.sign_in.profile

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.driveby.components.SmallSpacer
import com.example.driveby.components.TopBar
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.UserType
import com.example.driveby.navigation.Screen

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navigateToSignInScreen: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBar(
                title = Screen.ProfileScreen.route,
                signOut = {
                    viewModel.signOut()
                    navigateToSignInScreen()
                },
                revokeAccess = {
                    viewModel.revokeAccess()
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 48.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column {
                    if (viewModel.user != null) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            Text(viewModel.user!!.name + " " + viewModel.user!!.lastName)
                            SmallSpacer()
                            AsyncImage(
                                model = viewModel.user!!.imageUrl,
                                contentDescription = null,
                            )
                            SmallSpacer()
                            Text(
                                text = "Your score:" + viewModel.user?.score.toString(),
                                fontSize = 24.sp
                            )
                            SmallSpacer()
                        }
                    }

                    if (viewModel.user != null && viewModel.user!!.userType == UserType.Driver) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            Text("Your car")
                            Log.i(LOG_TAG, viewModel.user.toString())
                            val driver = viewModel.user as Driver
                            val car = driver.car

                            SmallSpacer()
                            Text(car.brand)
                            SmallSpacer()
                            Text(car.model)
                            SmallSpacer()
                            Text(car.seats.toString())
                            SmallSpacer()
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    )

    RevokeAccess(
        snackbarHostState = snackbarHostState,
        coroutineScope = coroutineScope,
        signOut = {
            viewModel.signOut()
            navigateToSignInScreen()
        }
    )
}
