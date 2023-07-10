package com.example.driveby.core

import android.content.Context
import android.location.Location.distanceBetween
import android.util.Log
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.Passenger
import com.example.driveby.domain.model.UserType
import com.google.firebase.database.DataSnapshot

class Utils {
    companion object {
        fun print(e: Exception) = Log.e(LOG_TAG, e.stackTraceToString())

        fun showToast(
            context: Context,
            message: String?
        ) = makeText(context, message, LENGTH_LONG).show()

        fun distance(
            startLatitude: Double,
            startLongitude: Double,
            endLatitude: Double,
            endLongitude: Double
        ): Double {
            val results = FloatArray(1)
            distanceBetween(
                startLatitude,
                startLongitude,
                endLatitude,
                endLongitude,
                results
            )

            return results[0].toDouble()
        }

    fun snapshotToIUser(userSnapshot: DataSnapshot) =
        when ((userSnapshot.value as HashMap<String, *>)["userType"]) {
            UserType.Passenger.name -> userSnapshot.getValue(Passenger::class.java)!!
            UserType.Driver.name -> userSnapshot.getValue(Driver::class.java)!!
            else -> userSnapshot.getValue(Passenger::class.java)!!
        }
    }
}
