package com.example.driveby.presentation.leaderboard

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.core.Utils.Companion.snapshotToUser
import com.example.driveby.domain.model.User
import com.example.driveby.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepo: UserRepository

) : ViewModel() {
    val leaderboard = MutableStateFlow<MutableList<User>>(mutableListOf())

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() = viewModelScope.launch {
        val usersSnapshot = userRepo.users.get().await()

        leaderboard.update {
            usersSnapshot.children.mapNotNull {
                snapshotToUser(it)
            }.toMutableStateList()
        }

        leaderboard.value.sortByDescending { it.score }
    }
}
