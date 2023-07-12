package com.example.driveby.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.core.Utils.Companion.distance
import com.example.driveby.core.Utils.Companion.snapshotToUser
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.SearchFilters
import com.example.driveby.domain.model.User
import com.example.driveby.domain.model.UserType
import com.example.driveby.domain.repository.AuthRepository
import com.example.driveby.domain.repository.UserRepository
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val userRepo: UserRepository,
    val authRepo: AuthRepository
) : ViewModel() {
    var usersOnMap = MutableStateFlow<MutableList<User>>(mutableListOf())
    var currentUser = MutableStateFlow<User?>(null)

    val showRatingDialog = MutableStateFlow<Boolean>(false)

    // Used for passenger to rate when drive ends.
    val drivenBy = MutableStateFlow<Driver?>(null)

    private val currentUserListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            // snapshot value may be null
            if (snapshot.value != null) {
                Log.i(LOG_TAG, snapshot.value.toString())
                currentUser.value = snapshotToUser(snapshot)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w(LOG_TAG, "usersListener:onCancelled", error.toException())
        }
    }

    private val passengerInTransitListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            // snapshot value may be null
            if (snapshot.value != null) {
                val inTransit = snapshot.value as Boolean

                if (!inTransit) {
                    Log.i(LOG_TAG, "Time for ratings")
                    showRatingDialog.update { true }

                    viewModelScope.launch {
                        userRepo.users
                            .child(authRepo.currentUser!!.uid)
                            .child("inTransit")
                            .removeValue()
                    }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w(LOG_TAG, "usersListener:onCancelled", error.toException())
        }
    }

    init {
        loadCurrentUser()
        userRepo.users
            .child(authRepo.currentUser!!.uid)
            .addValueEventListener(currentUserListener)

        userRepo.users
            .child(authRepo.currentUser!!.uid)
            .child("inTransit")
            .addValueEventListener(passengerInTransitListener)
    }

    override fun onCleared() {
        super.onCleared()
        userRepo.users.child(authRepo.currentUser!!.uid).removeEventListener(currentUserListener)
    }

    fun updateUserLocation(location: LocationResult) {
        val userId = authRepo.currentUser?.uid

        if (userId != null) {
            userRepo.users.child(userId).child("latitude")
                .setValue(location.lastLocation!!.latitude)
            userRepo.users.child(userId).child("longitude")
                .setValue(location.lastLocation!!.longitude)
        }
    }

    private fun loadCurrentUser() = viewModelScope.launch {
        val res = userRepo.users.child(authRepo.currentUser!!.uid).get().await()

        currentUser.value = snapshotToUser(res)
    }

    fun loadFilteredUsers(searchFilters: SearchFilters) = viewModelScope.launch {
        val currentUserSnapshot = userRepo.users.child(authRepo.currentUser!!.uid).get().await()

        Log.i(LOG_TAG, "Filtering")

        val currentUser: User =
            snapshotToUser(currentUserSnapshot)

        val usersSnapshot = userRepo.users.get().await()

        val usersToFilter = usersSnapshot.children.mapNotNull {
            Log.i(LOG_TAG, it.toString())
            snapshotToUser(it)
        } as MutableCollection<User>

        usersOnMap.value = when (currentUser.userType) {
            UserType.Passenger -> usersToFilter.filter {
                val isDriver = it.userType == UserType.Driver
                val hasRatings = isDriver && (it as Driver).ratingsCount > 0
                val meetsSeats = isDriver && (it as Driver).car.seats >= searchFilters.seats

                val meetsDistance = distance(
                    currentUser.latitude,
                    currentUser.longitude,
                    it.latitude,
                    it.longitude
                ).toInt() <= searchFilters.radius

                val isNotInTransit = isDriver && !(it as Driver).drive.active

                val meetsQuery = when (searchFilters.query.isNotBlank()) {
                    true -> when (it.userType) {
                        UserType.Passenger -> (it.name.contains(
                            searchFilters.query,
                            ignoreCase = true
                        ) || it.lastName.contains(searchFilters.query, ignoreCase = true))

                        UserType.Driver -> (it.name.contains(
                            searchFilters.query,
                            ignoreCase = true
                        ) || it.lastName.contains(
                            searchFilters.query,
                            ignoreCase = true
                        ) || (it as Driver).car.model.contains(
                            searchFilters.query,
                            ignoreCase = true
                        ) || (it as Driver).car.brand.contains(
                            searchFilters.query,
                            ignoreCase = true
                        ))
                    }

                    false -> true
                }

                when (hasRatings) {
                    true -> (it as Driver).ratingsSum / it.ratingsCount >= searchFilters.stars
                            && meetsSeats
                            && meetsDistance
                            && meetsQuery
                            && isNotInTransit

                    false -> isDriver && meetsSeats && meetsDistance && meetsQuery && isNotInTransit
                }
            }.toMutableList()

            UserType.Driver -> usersToFilter.filter {
                val meetsDistance = distance(
                    currentUser.latitude,
                    currentUser.longitude,
                    it.latitude,
                    it.longitude
                ).toInt() <= searchFilters.radius

                val meetsQuery = when (searchFilters.query.isNotBlank()) {
                    true -> when (it.userType) {
                        UserType.Passenger -> (it.name.contains(
                            searchFilters.query,
                            ignoreCase = true
                        ) || it.lastName.contains(searchFilters.query, ignoreCase = true))

                        UserType.Driver -> false
                    }

                    false -> true
                }

                it.userType == UserType.Passenger && meetsDistance && meetsQuery
            }.toMutableList()
        }
    }

    // should be called by passenger only
    fun addPassengerToDrive(selectedDriver: Driver) = viewModelScope.launch {
        if (selectedDriver.car.seats == 0) {
            return@launch
        }

        if (selectedDriver.drive.passengers.contains(currentUser.value!!.id)) {
            return@launch
        }

        drivenBy.update { selectedDriver }

        selectedDriver.car.seats--
        userRepo.users
            .child(selectedDriver.id)
            .child("car")
            .child("seats")
            .setValue(selectedDriver.car.seats).await()

        userRepo.users
            .child(selectedDriver.id)
            .child("drive")
            .child("passengers")
            .child(currentUser.value!!.id)
            .setValue(currentUser.value!!).await()
    }

    // should be called by driver only
    fun startDrive() = viewModelScope.launch {
        val driverSnapshot = userRepo.users.child(authRepo.currentUser!!.uid).get().await()
        val currentDriver = driverSnapshot.getValue(Driver::class.java)!!


        val drive = mapOf<String, Any>(
            "active" to true,
            "startLatitude" to currentDriver.latitude,
            "startLongitude" to currentDriver.longitude,
            "endLatitude" to -1.0,
            "endLongitude" to -1.0
        )

        for (passenger in currentDriver.drive.passengers.values) {
            userRepo.users
                .child(passenger.id)
                .child("inTransit")
                .setValue(true).await()
        }

        userRepo.users.child(currentDriver.id).child("drive").updateChildren(drive).await()
    }

    // should be called by driver only
    fun endDrive() = viewModelScope.launch {
        val driverSnapshot = userRepo.users.child(authRepo.currentUser!!.uid).get().await()
        val currentDriver = driverSnapshot.getValue(Driver::class.java)!!
        currentDriver.drive.active = false
        currentDriver.drive.endLatitude = currentDriver.latitude
        currentDriver.drive.endLongitude = currentDriver.longitude

        currentDriver.car.seats += currentDriver.drive.passengers.count()

        val distance = distance(
            currentDriver.drive.startLatitude,
            currentDriver.drive.startLongitude,
            currentDriver.drive.endLatitude,
            currentDriver.drive.endLongitude
        ).toInt()

        currentDriver.score += distance

        currentDriver.drive.passengers.values.forEach {
            it.score += distance
            userRepo.users.child(it.id).setValue(it)
        }

        for (passenger in currentDriver.drive.passengers.values) {
            userRepo.users
                .child(passenger.id)
                .child("inTransit")
                .setValue(false)
        }

        currentDriver.drive.passengers.clear()
        userRepo.users.child(currentDriver.id).setValue(currentDriver)
    }

    fun rateDriver(stars: Int, driver: Driver) = viewModelScope.launch {
        userRepo.users
            .child(driver.id)
            .child("ratingsSum")
            .setValue(driver.ratingsSum + stars)
            .await()

        userRepo.users
            .child(driver.id)
            .child("ratingsCount")
            .setValue(driver.ratingsCount + 1)
            .await()
    }
}
