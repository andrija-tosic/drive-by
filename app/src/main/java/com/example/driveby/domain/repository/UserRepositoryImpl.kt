package com.example.driveby.domain.repository

import android.util.Log
import com.example.driveby.core.Constants.LOG_TAG
import com.example.driveby.domain.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepositoryImpl @Inject constructor() :
    UserRepository {

    override val users = FirebaseDatabase.getInstance().reference.child("users")

    init {
        val driversListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: HashMap<String, User> = snapshot.value as HashMap<String, User>

                Log.i(LOG_TAG, "User updated: $user")

                // TODO: update map with this data
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(LOG_TAG, "loadDriver:onCancelled", error.toException())
            }
        }
        users.addValueEventListener(driversListener)
    }

    override suspend fun createUser(user: User) {
        users.child(user.id).setValue(user).await()
    }
}
