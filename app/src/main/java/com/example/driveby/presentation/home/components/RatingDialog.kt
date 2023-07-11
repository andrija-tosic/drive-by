package com.example.driveby.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.driveby.components.SmallSpacer
import com.example.driveby.components.UserListItem
import com.example.driveby.domain.model.Driver

@Composable
fun RatingDialog(
    driver: Driver,
    onClick: (stars: Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(Dp.Hairline, Color.Black)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Rate the driver")
                UserListItem(
                    user = driver,
                    title = "${driver.name} ${driver.lastName}\n",
                    subtitle = ""
                )

                var stars by remember { mutableIntStateOf(5) }

                Slider(
                    value = stars.toFloat(),
                    onValueChange = { newValue ->
                        stars = newValue.toInt()
                    },
                    valueRange = 1f..5f,
                    steps = 5,
                )

                Button(onClick = { onClick(stars) }) {
                    Icon(
                        Icons.Outlined.StarRate,
                        contentDescription = null
                    )
                    SmallSpacer()
                    SmallSpacer()
                    Text("Rate")
                }
            }
        }
    }
}