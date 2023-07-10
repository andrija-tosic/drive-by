package com.example.driveby.presentation.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.core.Utils.Companion.distance
import com.example.driveby.core.Utils.Companion.snapshotToIUser
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.IUser
import com.example.driveby.domain.model.SearchFilters
import com.example.driveby.domain.model.UserType
import com.example.driveby.domain.repository.AuthRepository
import com.example.driveby.domain.repository.UserRepository
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository
) : ViewModel() {
    var usersOnMap = mutableListOf<IUser>()
    var currentUser by mutableStateOf<IUser?>(null)

    private val usersListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            // snapshot value may be null
            if (snapshot.value != null) {
                val user: HashMap<String, IUser> = snapshot.value as HashMap<String, IUser>

                Log.i(LOG_TAG, "User updated: $user")

                loadFilteredUsers(SearchFilters())
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w(LOG_TAG, "usersListener:onCancelled", error.toException())
        }
    }

    private val currentUserListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            // snapshot value may be null
            if (snapshot.value != null) {
                val user: HashMap<String, IUser> = snapshot.value as HashMap<String, IUser>

                Log.i(LOG_TAG, "User updated: $user")

                loadCurrentUser()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w(LOG_TAG, "usersListener:onCancelled", error.toException())
        }
    }

    init {
        loadFilteredUsers(SearchFilters())
        loadCurrentUser()
        userRepo.users.addValueEventListener(usersListener)
        userRepo.users.child(authRepo.currentUser!!.uid).addValueEventListener(currentUserListener)
    }

    override fun onCleared() {
        super.onCleared()
        userRepo.users.removeEventListener(usersListener)
        userRepo.users.child(authRepo.currentUser!!.uid).removeEventListener(currentUserListener)
    }

    fun updateUserLocation(location: LocationResult) {
        val userId = authRepo.currentUser?.uid

        if (userId != null) {
            Log.i(LOG_TAG, "updating user $userId location to " + location.lastLocation)
            userRepo.users.child(userId).child("latitude")
                .setValue(location.lastLocation!!.latitude)
            userRepo.users.child(userId).child("longitude")
                .setValue(location.lastLocation!!.longitude)
        }
    }

    private fun loadCurrentUser() = viewModelScope.launch {
        val res = userRepo.users.child(authRepo.currentUser!!.uid).get().await()

        currentUser = snapshotToIUser(res)

        Log.i(LOG_TAG, currentUser.toString())
    }

    fun loadFilteredUsers(searchFilters: SearchFilters) = viewModelScope.launch {
        val currentUserSnapshot = userRepo.users.child(authRepo.currentUser!!.uid).get().await()

        val currentUser: IUser =
            snapshotToIUser(currentUserSnapshot)

        val usersSnapshot = userRepo.users.get().await()

        val usersToFilter = usersSnapshot.children.mapNotNull {
            snapshotToIUser(it)
        } as MutableCollection<IUser>

        usersOnMap = when (currentUser.userType) {
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

                val meetsQuery =
                    when (searchFilters.query.isNotBlank()) {
                        true -> (it.name.contains(
                            searchFilters.query,
                            ignoreCase = true
                        ) || it.lastName.contains(searchFilters.query, ignoreCase = true))

                        false -> true
                    }

                Log.i(LOG_TAG, "$isDriver $meetsSeats $meetsDistance $meetsQuery $isNotInTransit")

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
                it.userType == UserType.Passenger
            }.toMutableList()

        }
        Log.i(LOG_TAG, currentUser.userType.name + ", " + usersOnMap.toString())
    }

    // should be called by passenger only
    fun addPassengerToDrive(selectedDriver: Driver) = viewModelScope.launch {
        userRepo.users
            .child(selectedDriver.id)
            .child("drive")
            .child("passengers")
            .child(currentUser!!.id).setValue(currentUser)
    }

    // should be called by driver only
    fun startDrive() = viewModelScope.launch {
        val driverSnapshot = userRepo.users.child(authRepo.currentUser!!.uid).get().await()
        val currentDriver = driverSnapshot.getValue(Driver::class.java)!!

        val map = mapOf<String, Any>(
            "active" to true,
            "startLatitude" to currentDriver.latitude,
            "startLongitude" to currentDriver.longitude,
            "endLatitude" to -1.0,
            "endLongitude" to -1.0
        )

        userRepo.users.child(currentDriver.id).child("drive").updateChildren(map)
    }

    // should be called by driver only
    fun endDrive() = viewModelScope.launch {
        val driverSnapshot = userRepo.users.child(authRepo.currentUser!!.uid).get().await()
        val currentDriver = driverSnapshot.getValue(Driver::class.java)!!
        currentDriver.drive.active = false
        currentDriver.drive.endLatitude = currentDriver.latitude
        currentDriver.drive.endLongitude = currentDriver.longitude

        val distance = distance(
            currentDriver.drive.startLatitude,
            currentDriver.drive.startLongitude,
            currentDriver.drive.endLatitude,
            currentDriver.drive.endLongitude
        ).toInt()

        currentDriver.score += distance

        currentDriver.drive.passengers.forEach {
            it.score += distance
            userRepo.users.child(it.id).setValue(it)
        }

        userRepo.users.child(currentDriver.id).setValue(currentDriver)
    }
}
