package com.example.driveby.core

import android.content.Context
import android.location.Location.distanceBetween
import android.util.Log
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import com.example.driveby.core.Strings.LOG_TAG

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
    }
}
