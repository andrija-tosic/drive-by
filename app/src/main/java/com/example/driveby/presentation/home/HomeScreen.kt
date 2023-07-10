package com.example.driveby.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.SwipeDownAlt
import androidx.compose.material.icons.outlined.SwipeUpAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.sharp.MyLocation
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.driveby.components.LocationUpdates
import com.example.driveby.components.SmallSpacer
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.SearchFilters
import com.example.driveby.domain.model.UserType
import com.example.driveby.presentation.sign_in.profile.UserListItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class
)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val nis = LatLng(43.32, 21.89)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(nis, 10f)
    }
    var currentLocation: Location? = null

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = BottomSheetValue.Expanded
        )
    )

    if (ActivityCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    var showDriverInfoDialog by remember { mutableStateOf(false) }
    var selectedDriver: Driver? = null

    LocationUpdates(onLocationUpdate = {
        Log.i(LOG_TAG, it.toString())
        currentLocation = it.lastLocation
        viewModel.updateUserLocation(it)
    })



    Scaffold(content = { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            BottomSheetScaffold(
                sheetContent = {
                    if (viewModel.currentUser?.userType == UserType.Passenger) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            var query by rememberSaveable { mutableStateOf("") }
                            var active by rememberSaveable { mutableStateOf(false) }

                            Icon(
                                Icons.Outlined.SwipeUpAlt,
                                contentDescription = null
                            )


                            DockedSearchBar(
                                active = active,
                                onActiveChange = { active = it },
                                onQueryChange = { query = it },
                                onSearch = { active = false },
                                query = query,
                                placeholder = { Text("Search") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = null
                                    )
                                },
                            ) {}

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                val radiusRange = 0F..1000F
                                val radius =
                                    remember { mutableIntStateOf(radiusRange.endInclusive.toInt()) }
                                SmallSpacer()
                                Text("Radius")
                                SmallSpacer()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${radiusRange.start.toInt()} m")
                                    Text(text = "${radiusRange.endInclusive.toInt()} m")
                                }

                                Slider(
                                    value = radius.value.toFloat(),
                                    onValueChange = { newValue ->
                                        radius.value = newValue.toInt()
                                    },
                                    valueRange = radiusRange,
                                    steps = 20,
                                )
                                Text(text = "${radius.intValue} m")

                                val seats = remember { mutableIntStateOf(5) }
                                SmallSpacer()
                                Text("Number of seats")
                                SmallSpacer()

                                Slider(
                                    value = seats.value.toFloat(),
                                    onValueChange = { newValue ->
                                        seats.value = newValue.toInt()
                                    },
                                    valueRange = 1F..5F,
                                    steps = 5,
                                )
                                Text(text = "${seats.intValue} seats")

                                val rating = remember { mutableIntStateOf(4) }
                                SmallSpacer()
                                Text("Driver rating")
                                SmallSpacer()

                                Slider(
                                    value = rating.value.toFloat(),
                                    onValueChange = { newValue ->
                                        rating.value = newValue.toInt()
                                    },
                                    valueRange = 1F..5F,
                                    steps = 5,
                                )
                                Text(text = "${rating.intValue} stars")

                                SmallSpacer()
                                Button(onClick = {
                                    viewModel.loadFilteredUsers(
                                        SearchFilters(
                                            query,
                                            radius.value,
                                            seats.value,
                                            rating.value
                                        )
                                    )
                                }) {
                                    Icon(
                                        Icons.Outlined.FilterAlt,
                                        contentDescription = null
                                    )
                                    SmallSpacer()
                                    Text("Filter")
                                }
                                SmallSpacer()

                                Icon(
                                    Icons.Outlined.SwipeDownAlt,
                                    contentDescription = null
                                )
                            }

                        }

                    }
                },
                scaffoldState = bottomSheetScaffoldState
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 0.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            mapToolbarEnabled = true,
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false
                        ),
                        properties = MapProperties(
                            isMyLocationEnabled = true
                        )
                    ) {
                        MapEffect {
                            it.setOnCameraIdleListener {
                                scope.launch {
                                    if (viewModel.currentUser?.userType == UserType.Driver
                                        && (viewModel.currentUser as Driver).drive.active && currentLocation != null
                                    ) {
                                        cameraPositionState.animate(
                                            update = CameraUpdateFactory.newLatLngZoom(
                                                LatLng(
                                                    currentLocation!!.latitude,
                                                    currentLocation!!.longitude
                                                ),
                                                16f
                                            ),
                                            durationMs = 667
                                        )
                                    }
                                }
                            }
                        }
                        if (currentLocation != null) {
                            Circle(
                                center = LatLng(
                                    currentLocation!!.latitude,
                                    currentLocation!!.longitude
                                ),
                                fillColor = Color.Blue.copy(alpha = 0.67f),
                                visible = true
                            )
                        }

                        viewModel.usersOnMap.forEach { user ->
                            if (viewModel.currentUser?.id != user.id) {
                                Marker(
                                    MarkerState(LatLng(user.latitude, user.longitude)),
                                    onClick = {
                                        if (user.userType == UserType.Driver) {
                                            selectedDriver = user as Driver
                                            showDriverInfoDialog = true
                                            Log.i(LOG_TAG, "hi")
                                            Log.i(LOG_TAG, selectedDriver.toString())
                                            Log.i(LOG_TAG, showDriverInfoDialog.toString())
                                        }

                                        true
                                    },
                                    title = when (user.userType) {
                                        UserType.Passenger -> "Passenger ${user.name} ${user.lastName}"
                                        UserType.Driver -> "Driver ${user.name} ${user.lastName}\n" +
                                                "Score: ${user.score}\n" +
                                                "Car: ${(user as Driver).car.brand} ${user.car.model} (${user.car.seats} seater)"
                                    }
                                )
                            }
                        }
                    }


                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 92.dp, end = 32.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                scope.launch {
                                    if (currentLocation != null) {
                                        cameraPositionState.animate(
                                            update = CameraUpdateFactory.newLatLngZoom(
                                                LatLng(
                                                    currentLocation!!.latitude,
                                                    currentLocation!!.longitude
                                                ),
                                                16f
                                            ),
                                            durationMs = 667
                                        )
                                    }
                                }
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.BottomEnd),
                            content = {
                                Icon(
                                    modifier = Modifier.size(25.dp),
                                    imageVector = Icons.Sharp.MyLocation,
                                    contentDescription = "Location",
                                    tint = Color.DarkGray
                                )
                            }
                        )
                    }


                    if (showDriverInfoDialog) {
                        Dialog(onDismissRequest = { showDriverInfoDialog = false }) {
                            Surface(
                                modifier = Modifier.align(Alignment.Center),
                                onClick = { showDriverInfoDialog = false },
                                shape = RectangleShape,
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(Dp.Hairline, Color.Black),
                            ) {
                                UserListItem(
                                    photoUrl = selectedDriver!!.imageUrl,
                                    title = "Driver ${selectedDriver!!.name} ${selectedDriver!!.lastName}\n" +
                                            "Score: ${selectedDriver!!.score}\n" +
                                            "Car: ${selectedDriver!!.car.brand} ${selectedDriver!!.car.model} (${selectedDriver!!.car.seats} seater)",
                                    subtitle = "Subtitle"
                                )

//                                Box(
//                                    Modifier
//                                        .padding(16.dp)
//                                        .background(MaterialTheme.colorScheme.background)
//                                        .align(Alignment.Center)
//                                ) {
//                                    if (selectedDriver != null) {
//                                        Column {
//                                            Text(
//                                                "Driver ${selectedDriver!!.name} ${selectedDriver!!.lastName}\n" +
//                                                        "Score: ${selectedDriver!!.score}\n" +
//                                                        "Car: ${selectedDriver!!.car.brand} ${selectedDriver!!.car.model} (${selectedDriver!!.car.seats} seater)"
//                                            )
                                SmallSpacer()
                                Button(onClick = {
                                    viewModel.addPassengerToDrive(
                                        selectedDriver!!
                                    )
                                }) {
                                    Text("Request a drive")
                                }
//                                        }
//                                    }
//                                }
                            }
                        }
                    }
                }
            }
        }
    },
        floatingActionButton = {
            if (viewModel.currentUser?.userType == UserType.Driver) {
                ExtendedFloatingActionButton(
                    text = {
                        when ((viewModel.currentUser as Driver).drive.active) {
                            true -> Text("End drive")
                            false -> Text("Start drive")
                        }
                    },
                    icon = {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = "Drive toggle",
                        )
                    },
                    onClick = {
                        when ((viewModel.currentUser as Driver).drive.active) {
                            true -> viewModel.endDrive()
                            false -> viewModel.startDrive()
                        }
                    },
                    expanded = true,
                )
            }
        }
    )
}
