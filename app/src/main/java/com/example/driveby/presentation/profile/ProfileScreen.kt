package com.example.driveby.presentation.profile

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.driveby.components.TopBar
import com.example.driveby.components.UserListItem
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.UserType
import com.example.driveby.navigation.Screen
import com.example.driveby.presentation.profile.components.RevokeAccess

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navigateToSignInScreen: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val user by viewModel.user.collectAsState()

    Scaffold(
        topBar = {
            TopBar(
                title = Screen.ProfileScreen.route,
                signOut = {
                    viewModel.signOut()
                    navigateToSignInScreen()
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (user != null) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            UserListItem(
                                user!!,
                                user!!.name + " " + user!!.lastName,
                                "${user!!.score} points",
                                imageSize = 128.dp
                            )
                        }

                        if (user!!.userType == UserType.Driver) {
                            val driver = user as Driver

                            Row(horizontalArrangement = Arrangement.Center) {
                                if (driver.ratingsCount > 0) {
                                    Text(
                                        text = "Your rating: " + driver.ratingsSum / driver.ratingsCount + "/5"
                                    )
                                }
                            }

                            val car = driver.car
                            Log.i(LOG_TAG, user.toString())

                            Row {
                                Text("Your car: ")
                                Text(car.brand + " ")
                                Text(car.model + " ")
                                Text("(${car.seats} seats)")
                            }
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
