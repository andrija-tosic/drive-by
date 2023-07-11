package com.example.driveby.presentation.sign_up.components

import CameraCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveby.components.EmailField
import com.example.driveby.components.PasswordField
import com.example.driveby.components.SmallSpacer
import com.example.driveby.components.TextField
import com.example.driveby.core.Strings.ALREADY_USER
import com.example.driveby.core.Strings.SIGN_UP_BUTTON
import com.example.driveby.core.Utils.Companion.showToast
import com.example.driveby.domain.model.UserType

@Composable
@ExperimentalComposeUiApi
fun SignUpContent(
    padding: PaddingValues,
    signUpPassenger: (
        email: String,
        password: String,
        phone: String,
        firstName: String,
        lastName: String,
        userType: UserType,
        photoUri: String
    ) -> Unit,
    signUpDriver: (
        email: String,
        password: String,
        phone: String,
        firstName: String,
        lastName: String,
        userType: UserType,
        photoUri: String,
        carModel: String,
        carBrand: String,
        carSeats: Int
    ) -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    var email by rememberSaveable(
        stateSaver = TextFieldValue.Saver,
        init = {
            mutableStateOf(
                value = TextFieldValue(
                    text = "andonnet@gmail.com"
                )
            )
        }
    )
    var password by rememberSaveable(
        stateSaver = TextFieldValue.Saver,
        init = {
            mutableStateOf(
                value = TextFieldValue(
                    text = "password"
                )
            )
        }
    )
    val keyboard = LocalSoftwareKeyboardController.current

    var phone by rememberSaveable(
        stateSaver = TextFieldValue.Saver,
        init = {
            mutableStateOf(
                value = TextFieldValue(
                    text = "0665063636"
                )
            )
        }
    )

    var firstName by rememberSaveable(
        stateSaver = TextFieldValue.Saver,
        init = {
            mutableStateOf(
                value = TextFieldValue(
                    text = "Andrija"
                )
            )
        }
    )

    var lastName by rememberSaveable(
        stateSaver = TextFieldValue.Saver,
        init = {
            mutableStateOf(
                value = TextFieldValue(
                    text = "Tošić"
                )
            )
        }
    )

    var userType by rememberSaveable(
        init = {
            mutableStateOf(
                value = UserType.Passenger
            )
        }
    )

    var imageUri: String? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmailField(
            email = email,
            onEmailValueChange = { newValue -> email = newValue }
        )
        SmallSpacer()
        PasswordField(
            password = password,
            onPasswordValueChange = { newValue -> password = newValue }
        )
        SmallSpacer()
        TextField(
            text = firstName,
            keyboardType = KeyboardType.Text,
            onTextValueChange = { newValue -> firstName = newValue },
            label = "First name"
        )
        SmallSpacer()
        TextField(
            text = lastName,
            keyboardType = KeyboardType.Text,
            onTextValueChange = { newValue -> lastName = newValue },
            label = "Last name"
        )
        SmallSpacer()
        TextField(
            text = phone,
            keyboardType = KeyboardType.Phone,
            onTextValueChange = { newValue -> phone = newValue },
            label = "Phone number"
        )
        SmallSpacer()
        Text(text = "User type")
        SmallSpacer()
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = userType == UserType.Passenger,
                onClick = { userType = UserType.Passenger }
            )
            Text(text = UserType.Passenger.name)
            SmallSpacer()
            RadioButton(
                selected = userType == UserType.Driver,
                onClick = { userType = UserType.Driver }
            )
            Text(text = UserType.Driver.name)
        }
        SmallSpacer()
        CameraCapture { uri -> imageUri = uri.toString() }
        SmallSpacer()


        var brand by rememberSaveable(
            stateSaver = TextFieldValue.Saver,
            init = {
                mutableStateOf(
                    value = TextFieldValue(
                        text = "BMW"
                    )
                )
            }
        )
        var model by rememberSaveable(
            stateSaver = TextFieldValue.Saver,
            init = {
                mutableStateOf(
                    value = TextFieldValue(
                        text = "520i"
                    )
                )
            }
        )

        var seats by rememberSaveable(
            stateSaver = TextFieldValue.Saver,
            init = {
                mutableStateOf(
                    value = TextFieldValue(
                        text = "5"
                    )
                )
            }
        )
        if (userType == UserType.Driver) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .border(0.dp, Color.White, RoundedCornerShape(8.dp)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SmallSpacer()
                Text(
                    text = "Your car",
                    fontSize = 16.sp
                )
                SmallSpacer()
                TextField(
                    text = brand,
                    keyboardType = KeyboardType.Text,
                    onTextValueChange = { newValue -> brand = newValue },
                    label = "Brand"
                )
                SmallSpacer()
                TextField(
                    text = model,
                    keyboardType = KeyboardType.Text,
                    onTextValueChange = { newValue -> model = newValue },
                    label = "Model"
                )
                SmallSpacer()
                TextField(
                    text = seats,
                    keyboardType = KeyboardType.Number,
                    onTextValueChange = { newValue ->
                        if (newValue.text.toInt() in 1..7) {
                            seats = newValue
                        }
                    },
                    label = "Number of seats"
                )
            }
        }

        Button(
            onClick = {
                if (imageUri.isNullOrBlank()) {
                    showToast(context, "Take a picture first.")
                    return@Button
                }

                keyboard?.hide()

                when (userType) {
                    UserType.Passenger -> signUpPassenger(
                        email.text,
                        password.text,
                        phone.text,
                        firstName.text,
                        lastName.text,
                        userType,
                        imageUri!!
                    )

                    UserType.Driver -> signUpDriver(
                        email.text,
                        password.text,
                        phone.text,
                        firstName.text,
                        lastName.text,
                        userType,
                        imageUri!!,
                        model.text,
                        brand.text,
                        seats.text.toInt()
                    )
                }


            }
        ) {
            Text(
                text = SIGN_UP_BUTTON,
                fontSize = 15.sp
            )
        }
        Text(
            modifier = Modifier.clickable {
                navigateBack()
            },
            text = ALREADY_USER,
            fontSize = 15.sp
        )
    }
}
