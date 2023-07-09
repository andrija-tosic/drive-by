package com.example.driveby.domain.repository

import com.example.driveby.domain.model.IUser
import com.google.firebase.database.DatabaseReference

interface UserRepository {
    val users: DatabaseReference
    suspend fun createUser(user: IUser)

}