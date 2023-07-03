package com.example.driveby.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.driveby.core.Constants.LOG_TAG
import com.example.driveby.domain.repository.AuthRepository
import com.example.driveby.domain.repository.UserRepository
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository
) : ViewModel() {
    fun updateUserLocation(location: LocationResult) {
        val userId = authRepo.currentUser?.uid

        if (userId != null) {
            Log.i(LOG_TAG, "updating user ${userId} location to " + location.lastLocation)
            userRepo.users.child(userId).child("latitude").setValue(location.lastLocation!!.latitude)
            userRepo.users.child(userId).child("longitude").setValue(location.lastLocation!!.longitude)
        }
    }
}
