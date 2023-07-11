package com.example.driveby.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.driveby.components.SmallSpacer
import com.example.driveby.components.UserListItem
import com.example.driveby.domain.model.Driver

@Composable
fun DriverDialog(
    selectedDriver: Driver,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(Dp.Hairline, Color.Black)
        ) {
            val ratingString = when (selectedDriver.ratingsCount) {
                0 -> "No ratings yet"
                else -> "Rating: ${selectedDriver.ratingsSum / selectedDriver.ratingsCount}/5"
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Driver")
                UserListItem(
                    user = selectedDriver,
                    title = "${selectedDriver.name} ${selectedDriver.lastName}\n",
                    subtitle = "$ratingString\n"
                )
                Text("Car")
                Text("${selectedDriver.car.brand} ${selectedDriver.car.model} (${selectedDriver.car.seats} seater)")
                SmallSpacer()

                Button(onClick = onClick) {
                    Icon(
                        Icons.Outlined.DirectionsCar,
                        contentDescription = null
                    )
                    SmallSpacer()
                    Text("Request a drive")
                }
            }
        }
    }
}
