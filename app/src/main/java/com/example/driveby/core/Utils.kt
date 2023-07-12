package com.example.driveby.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location.distanceBetween
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.core.content.ContextCompat
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.Passenger
import com.example.driveby.domain.model.UserType
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.database.DataSnapshot

class Utils {
    companion object {
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

    fun snapshotToUser(userSnapshot: DataSnapshot) =
        when ((userSnapshot.value as HashMap<String, *>)["userType"]) {
            UserType.Passenger.name -> userSnapshot.getValue(Passenger::class.java)!!
            UserType.Driver.name -> userSnapshot.getValue(Driver::class.java)!!
            else -> userSnapshot.getValue(Passenger::class.java)!!
        }
        fun bitmapDescriptorFromVector(
            context: Context,
            vectorResId: Int
        ): BitmapDescriptor? {

            val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            val bm = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bm)
            drawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bm)
        }
    }
}
