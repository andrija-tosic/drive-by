package com.example.driveby.presentation.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.core.Utils.Companion.distance
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.IUser
import com.example.driveby.domain.model.SearchFilters
import com.example.driveby.domain.model.User
import com.example.driveby.domain.model.UserType
import com.example.driveby.domain.repository.AuthRepository
import com.example.driveby.domain.repository.UserRepository
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository
) : ViewModel() {
    var drivers = mutableListOf<Driver>()
    var currentUser by mutableStateOf<IUser?>(null)

    init {
        loadFilteredUsers(SearchFilters())
        getUser()
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

    fun getUser() = viewModelScope.launch {
        val res = userRepo.users.child(authRepo.currentUser!!.uid).get().await()

        Log.i(LOG_TAG, res.value.toString())

        when ((res.value as HashMap<String, *>)["userType"]) {
            UserType.Passenger.name -> currentUser = res.getValue(User::class.java)
            UserType.Driver.name -> currentUser = res.getValue(Driver::class.java)
        }

        Log.i(LOG_TAG, currentUser.toString())
    }

    fun loadFilteredUsers(searchFilters: SearchFilters) = viewModelScope.launch {
        val currentUserSnapshot = userRepo.users.child(authRepo.currentUser!!.uid).get().await()

        val currentUser: IUser = when ((currentUserSnapshot.value as HashMap<String, *>)["userType"]) {
            UserType.Passenger.name -> currentUserSnapshot.getValue(User::class.java)!!
            UserType.Driver.name -> currentUserSnapshot.getValue(Driver::class.java)!!
            else -> currentUserSnapshot.getValue(User::class.java)!!
        }

        val usersSnapshot = userRepo.users.get().await()

        val drivers = usersSnapshot.children.mapNotNull {
            when ((currentUserSnapshot.value as HashMap<String, *>)["userType"]) {
                UserType.Passenger.name -> currentUserSnapshot.getValue(User::class.java)!!
                UserType.Driver.name -> currentUserSnapshot.getValue(Driver::class.java)!!
                else -> currentUserSnapshot.getValue(User::class.java)!!
            }
        }.filter { it.userType == UserType.Driver } as MutableCollection<Driver>

        this@HomeViewModel.drivers = when (currentUser.userType) {
            UserType.Passenger -> drivers.filter {
                it.score >= searchFilters.stars
                        && it.car.seats >= searchFilters.seats
                        && distance(
                    currentUser.latitude,
                    currentUser.longitude,
                    it.latitude,
                    it.longitude
                ).toInt() <= searchFilters.radius
            }.toMutableList()

            UserType.Driver -> drivers.toMutableList()
        }

        Log.i(LOG_TAG, this@HomeViewModel.drivers.toString())
    }

    // should be called by driver only
    fun startRide() = viewModelScope.launch {
        val driverSnapshot = userRepo.users.child(authRepo.currentUser!!.uid).get().await()
        val currentUser: IUser = driverSnapshot.getValue(Driver::class.java)!!
        userRepo.users.child(currentUser.id).child("drive").setValue(object {
            val active = true
            val startLatitude = currentUser.latitude
            val startLongitude = currentUser.longitude
            val endLatitude = null
            val endLongitude = null
        })
    }
}
