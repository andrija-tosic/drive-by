package com.example.driveby.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: AuthRepository
): ViewModel() {
    init {
        getAuthState()
    }

    fun getAuthState() = repo.isAuthenticated(viewModelScope)

    val isEmailVerified get() = repo.currentUser?.isEmailVerified ?: false
}
