package com.example.driveby.presentation.home.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.driveby.core.Strings
import com.example.driveby.core.Utils
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.User
import com.example.driveby.domain.model.UserType

@Composable
fun DriveFAB(
    currentUser: User?,
    context: Context,
    startDrive: () -> Unit,
    endDrive: () -> Unit
) {
    if (currentUser?.userType == UserType.Driver) {
        ExtendedFloatingActionButton(
            text = {
                when ((currentUser as Driver).drive.active) {
                    true -> Text("End drive")
                    false -> Text("Start drive")
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = "Drive toggle",
                )
            },
            onClick = {
                when ((currentUser as Driver).drive.active) {
                    true -> {
                        Utils.showToast(context, "Drive ended")
                        endDrive()
                    }

                    false -> {
                        val driver = currentUser as Driver
                        val allWithinRadius = driver.drive.passengers.values.all {
                            val dist = Utils.distance(
                                driver.latitude,
                                driver.longitude,
                                it.latitude,
                                it.longitude
                            )

                            Log.i(Strings.LOG_TAG, "Distance when starting drive: $dist")

                            dist <= 15f
                        }

                        if (driver.drive.passengers.isEmpty()) {
                            Utils.showToast(context, "No passengers in car.")
                        } else if (allWithinRadius) {
                            Utils.showToast(context, "Drive started")
                            startDrive()
                        } else {
                            Utils.showToast(
                                context,
                                "Not all passengers are near enough to start driving."
                            )
                        }
                    }
                }
            },
            expanded = true,
        )
    }
}
