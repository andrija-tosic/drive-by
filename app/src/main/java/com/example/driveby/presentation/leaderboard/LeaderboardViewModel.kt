package com.example.driveby.presentation.sign_in.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.core.Utils.Companion.snapshotToIUser
import com.example.driveby.domain.model.IUser
import com.example.driveby.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepo: UserRepository

) : ViewModel() {
    var leaderboard = mutableListOf<IUser>()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() = viewModelScope.launch {
        val usersSnapshot = userRepo.users.get().await()

        leaderboard = usersSnapshot.children.mapNotNull {
            snapshotToIUser(it)
        } as MutableList<IUser>

        leaderboard.sortBy { it.score }

        Log.i(LOG_TAG, leaderboard.toString())
    }
}
