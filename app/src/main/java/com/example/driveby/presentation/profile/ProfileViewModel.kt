package com.example.driveby.presentation.sign_in.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.core.Utils.Companion.snapshotToIUser
import com.example.driveby.domain.model.IUser
import com.example.driveby.domain.model.Response
import com.example.driveby.domain.repository.AuthRepository
import com.example.driveby.domain.repository.ReloadUserResponse
import com.example.driveby.domain.repository.RevokeAccessResponse
import com.example.driveby.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository

) : ViewModel() {
    var user by mutableStateOf<IUser?>(null)
    var revokeAccessResponse by mutableStateOf<RevokeAccessResponse>(Response.None)
        private set
    var reloadUserResponse by mutableStateOf<ReloadUserResponse>(Response.None)
        private set

    init {
        getUser()
    }

    private fun getUser() = viewModelScope.launch {
        val res = userRepo.users.child(authRepo.currentUser!!.uid).get().await()

        Log.i(LOG_TAG, res.value.toString())

        user = snapshotToIUser(res)
        Log.i(LOG_TAG, "Profile " + user.toString())
    }

    fun reloadUser() = viewModelScope.launch {
        reloadUserResponse = Response.Loading
        reloadUserResponse = authRepo.reloadFirebaseUser()
    }

    val isEmailVerified get() = authRepo.currentUser?.isEmailVerified ?: false

    fun signOut() = authRepo.signOut()

    fun revokeAccess() = viewModelScope.launch {
        revokeAccessResponse = Response.Loading
        revokeAccessResponse = authRepo.revokeAccess()
    }
}