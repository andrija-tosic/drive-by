package com.example.driveby.domain.repository

import com.example.driveby.domain.model.User

interface UserRepository {
    suspend fun createUser(user: User)
}