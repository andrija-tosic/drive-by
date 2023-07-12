package com.example.driveby.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.sharp.MyLocation
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.driveby.R
import com.example.driveby.components.LocationUpdates
import com.example.driveby.components.SmallSpacer
import com.example.driveby.components.UserListItem
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.core.Utils.Companion.bitmapDescriptorFromVector
import com.example.driveby.core.Utils.Companion.showToast
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.Passenger
import com.example.driveby.domain.model.SearchFilters
import com.example.driveby.domain.model.UserType
import com.example.driveby.presentation.home.components.DriveFAB
import com.example.driveby.presentation.home.components.DriverDialog
import com.example.driveby.presentation.home.components.PassengerDialog
import com.example.driveby.presentation.home.components.RatingDialog
import com.example.driveby.presentation.home.components.UsersDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(
    ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class, ExperimentalPermissionsApi::class
)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val nis = LatLng(43.32, 21.90)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(nis, 14f)
    }
    var currentLocation by remember {
        mutableStateOf<Location?>(null)
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = BottomSheetValue.Expanded
        )
    )

    val showRatingDialog = viewModel.showRatingDialog

    val showRatingDialogState by viewModel.showRatingDialog.collectAsState()

    val drivenBy = viewModel.drivenBy
    val drivenByState by viewModel.drivenBy.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    val radiusRange = 0F..1000F
    var radius by remember { mutableIntStateOf(radiusRange.endInclusive.toInt()) }
    var seats by remember { mutableIntStateOf(1) }
    var rating by remember { mutableIntStateOf(1) }

    val usersOnMap by viewModel.usersOnMap.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) {
//        viewModel.startLocationUpdates()
    }

    if (locationPermissionState.allPermissionsGranted) {
        LocationUpdates(onLocationUpdate = {
            currentLocation = it.lastLocation
            viewModel.updateUserLocation(it)
        })
    }

    LaunchedEffect(Unit) {
        locationPermissionState.launchMultiplePermissionRequest()
    }


    var showDriverInfoDialog by remember { mutableStateOf(false) }
    var selectedDriver: Driver? = null

    var showPassengerInfoDialog by remember { mutableStateOf(false) }
    var selectedPassenger: Passenger? = null

    var showUsersDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit)
    {
        val usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot value may be null
                if (snapshot.value != null) {
                    Log.i(LOG_TAG, "DisposableEffect, filtering")

                    viewModel.loadFilteredUsers(SearchFilters(query, radius, seats, rating))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(LOG_TAG, "usersListener:onCancelled", error.toException())
            }
        }
        viewModel.userRepo.users.addValueEventListener(usersListener)

        onDispose {
            Log.i(LOG_TAG, "onDispose")
            viewModel.userRepo.users.removeEventListener(usersListener)
        }
    }

    Scaffold(content = { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            BottomSheetScaffold(
                sheetBackgroundColor = MaterialTheme.colorScheme.background,
                sheetContent = {
//                    if (currentUser?.userType == UserType.Passenger) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        var active by rememberSaveable { mutableStateOf(false) }

                        Icon(
                            imageVector = when (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                                true -> Icons.Outlined.ExpandMore
                                false -> Icons.Outlined.ExpandLess
                            },
                            contentDescription = null
                        )
                        Row {
                            Icon(
                                Icons.Outlined.FilterAlt,
                                contentDescription = null
                            )
                            SmallSpacer()
                            Text("Filters")
                        }
                        SmallSpacer()
                        DockedSearchBar(
                            active = active,
                            onActiveChange = { active = it },
                            onQueryChange = {
                                query = it

                                viewModel.loadFilteredUsers(
                                    SearchFilters(
                                        query, radius, seats, rating
                                    )
                                )
                            },
                            onSearch = { active = false },
                            query = query,
                            placeholder = { Text("Search") },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = null
                                )
                            },
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                            ) {
                                items(usersOnMap.take(3)) { user ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        UserListItem(
                                            user,
                                            user.name + " " + user.lastName,
                                            "${user.score} points"
                                        )
                                    }
                                }
                            }
                        }
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
                            value = radius.toFloat(),
                            onValueChange = { newValue ->
                                radius = newValue.toInt()

                                viewModel.loadFilteredUsers(
                                    SearchFilters(
                                        query, radius, seats, rating
                                    )
                                )
                            },
                            valueRange = radiusRange,
                            steps = 20,
                        )
                        Text(text = "$radius m")

                        if (currentUser?.userType == UserType.Passenger) {

                            SmallSpacer()
                            Text("Number of seats")
                            SmallSpacer()

                            Slider(
                                value = seats.toFloat(),
                                onValueChange = { newValue ->
                                    seats = newValue.toInt()

                                    viewModel.loadFilteredUsers(
                                        SearchFilters(
                                            query, radius, seats, rating
                                        )
                                    )
                                },
                                valueRange = 1F..5F,
                                steps = 5,
                            )
                            Text(text = "$seats seats")

                            SmallSpacer()
                            Text("Driver rating")
                            SmallSpacer()

                            Slider(
                                value = rating.toFloat(),
                                onValueChange = { newValue ->
                                    rating = newValue.toInt()

                                    viewModel.loadFilteredUsers(
                                        SearchFilters(
                                            query, radius, seats, rating
                                        )
                                    )
                                },
                                valueRange = 1F..5F,
                                steps = 5,
                            )
                            Text(text = "$rating stars")
                        }
                    }
//                    }
                },
                scaffoldState = bottomSheetScaffoldState
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 0.dp)
                ) {
                    if (locationPermissionState.allPermissionsGranted) {

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
                                        if (currentUser?.userType == UserType.Driver
                                            && (currentUser as Driver).drive.active && currentLocation != null
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
                                    strokeWidth = 0.0f,
                                    fillColor = Color.Blue.copy(alpha = 0.1f),
                                    visible = true,
                                    radius = radius.toDouble()
                                )
                            }

                            usersOnMap.forEach { user ->
                                if (currentUser?.id != user.id) {
                                    Marker(
                                        state = MarkerState(
                                            LatLng(
                                                user.latitude,
                                                user.longitude
                                            )
                                        ),
                                        icon = when (user.userType) {
                                            UserType.Passenger -> bitmapDescriptorFromVector(
                                                context,
                                                R.drawable.baseline_person_pin_circle_36
                                            )

                                            UserType.Driver -> bitmapDescriptorFromVector(
                                                context,
                                                R.drawable.baseline_directions_car_36
                                            )
                                        },
                                        onClick = {
                                            when (user.userType) {
                                                UserType.Passenger -> {
                                                    selectedPassenger = user as Passenger
                                                    showPassengerInfoDialog = true
                                                    Log.i(LOG_TAG, selectedPassenger.toString())
                                                    Log.i(
                                                        LOG_TAG,
                                                        showPassengerInfoDialog.toString()
                                                    )
                                                }

                                                UserType.Driver -> {
                                                    selectedDriver = user as Driver
                                                    showDriverInfoDialog = true
                                                    Log.i(LOG_TAG, selectedDriver.toString())
                                                    Log.i(
                                                        LOG_TAG,
                                                        showDriverInfoDialog.toString()
                                                    )
                                                }
                                            }

                                            true
                                        },
                                        title = "${user.name} ${user.lastName}"
                                    )
                                }
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
                                .size(56.dp)
                                .align(Alignment.BottomEnd),
                            content = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Sharp.MyLocation,
                                    contentDescription = "Location"
                                )
                            }
                        )

                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 92.dp, start = 32.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                showUsersDialog = true
                            },
                            shape = CircleShape,
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.BottomStart),
                            content = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = "Location"
                                )
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
//                            .padding(bottom = 76.dp, end = 92.dp)
                    ) {
                        DriveFAB(
                            currentUser,
                            context,
                            { viewModel.startDrive() },
                            { viewModel.endDrive() })
                    }


                    if (showUsersDialog) {
                        UsersDialog(
                            users = usersOnMap,
                            onDismissRequest = { showUsersDialog = false })
                    }

                    if (showRatingDialogState && drivenByState != null && currentUser!!.userType == UserType.Passenger) {
                        RatingDialog(driver = drivenByState!!,
                            onClick = { stars ->
                                viewModel.rateDriver(stars, drivenByState!!)
                                showRatingDialog.update { false }
                                drivenBy.update { null }
                            },
                            onDismissRequest = { showRatingDialog.update { false } })
                    }

                    if (showDriverInfoDialog && selectedDriver != null) {
                        DriverDialog(
                            selectedDriver!!,
                            {
                                if (selectedDriver!!.car.seats == 0) {
                                    showToast(
                                        context,
                                        "No more free seats in this driver's car."
                                    )
                                } else if (selectedDriver!!.drive.passengers.contains(
                                        currentUser!!.id
                                    )
                                ) {
                                    showToast(
                                        context,
                                        "Already a part of this drive."
                                    )
                                } else {
                                    showToast(context, "Drive requested")
                                    viewModel.addPassengerToDrive(selectedDriver!!)
                                }

                                showDriverInfoDialog = false
                            },
                            { showDriverInfoDialog = false })
                    }
                    if (showPassengerInfoDialog && selectedPassenger != null) {
                        PassengerDialog(
                            selectedPassenger!!,
                            { showPassengerInfoDialog = false }
                        )
                    }

                }
            }
        }
    },
//        floatingActionButton = {
//            DriveFAB(currentUser, context, { viewModel.startDrive() }, { viewModel.endDrive() })
//        }
    )
}

