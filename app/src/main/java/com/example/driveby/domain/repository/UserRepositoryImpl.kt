package com.example.driveby.domain.repository

import com.example.driveby.domain.model.IUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor() :
    UserRepository {

    override val users = FirebaseDatabase.getInstance().reference.child("users")

    override suspend fun createUser(user: IUser) {
        users.child(user.id).setValue(user).await()
    }
}
