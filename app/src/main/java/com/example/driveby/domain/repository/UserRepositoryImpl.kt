package com.example.driveby.domain.repository

import com.example.driveby.domain.model.Response
import com.example.driveby.domain.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
) : UserRepository {
    private val users = FirebaseDatabase.getInstance().reference.child("users")

    override suspend fun createUser(user: User) {
        users.setValue(user).await()
    }
}
