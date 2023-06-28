package com.example.driveby.presentation.sign_up.components

import CameraCapture
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.example.driveby.components.EmailField
import com.example.driveby.components.PasswordField
import com.example.driveby.components.TextField
import com.example.driveby.components.SmallSpacer
import com.example.driveby.core.Constants.ALREADY_USER
import com.example.driveby.core.Constants.SIGN_UP_BUTTON
import com.example.driveby.core.Utils.Companion.showToast
import com.example.driveby.domain.model.UserType

@Composable
@ExperimentalComposeUiApi
fun SignUpContent(
    padding: PaddingValues,
    signUp: (
        email: String,
        password: String,
        phone: String,
        firstName: String,
        lastName: String,
        userType: UserType,
        photoUri: String
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
                value = UserType.Regular
            )
        }
    )

    var imageUri: String? = null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
        Row() {
            RadioButton(
                selected = userType == UserType.Regular,
                onClick = { userType = UserType.Regular })
            Text(text = "Regular")
            SmallSpacer()
            RadioButton(
                selected = userType == UserType.Driver,
                onClick = { userType = UserType.Driver })
            Text(text = "Driver")
        }
        SmallSpacer()
        CameraCapture { uri -> imageUri = uri.toString() }
        SmallSpacer()
        Button(
            onClick = {
                if (imageUri.isNullOrBlank()) {
                    showToast(context, "Take a picture first.")
                    return@Button
                }

                keyboard?.hide()
                signUp(
                    email.text,
                    password.text,
                    phone.text,
                    firstName.text,
                    lastName.text,
                    userType,
                    imageUri!!
                )
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
