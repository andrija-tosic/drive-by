package com.example.driveby.domain.repository

import android.util.Log
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.domain.model.IUser
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
        Log.i(LOG_TAG, "UserRepositoryImpl")

        val driversListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot value may be null

//                val user: HashMap<String, IUser> = snapshot.value as HashMap<String, IUser>

//                Log.i(LOG_TAG, "User updated: $user")

                // TODO: update map with this data
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(LOG_TAG, "loadDriver:onCancelled", error.toException())
            }
        }
        users.addValueEventListener(driversListener)
    }

    override suspend fun createUser(user: IUser) {
        users.child(user.id).setValue(user).await()
    }
}
