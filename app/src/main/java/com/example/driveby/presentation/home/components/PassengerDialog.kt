package com.example.driveby.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.driveby.components.UserListItem
import com.example.driveby.domain.model.Passenger

@Composable
 fun PassengerDialog(
    selectedPassenger: Passenger,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(Dp.Hairline, Color.Black)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Passenger")
                UserListItem(
                    user = selectedPassenger,
                    title = "${selectedPassenger.name} ${selectedPassenger.lastName}\n",
                    subtitle = "${selectedPassenger.score} points"
                )
            }
        }
    }
}
