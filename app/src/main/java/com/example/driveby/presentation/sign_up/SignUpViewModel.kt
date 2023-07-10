package com.example.driveby.presentation.sign_up

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.core.Strings.LOG_TAG
import com.example.driveby.domain.model.Car
import com.example.driveby.domain.model.Driver
import com.example.driveby.domain.model.Response
import com.example.driveby.domain.model.Passenger
import com.example.driveby.domain.model.UserType
import com.example.driveby.domain.repository.AuthRepository
import com.example.driveby.domain.repository.SendEmailVerificationResponse
import com.example.driveby.domain.repository.SignUpResponse
import com.example.driveby.domain.repository.UserRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {
    var signUpResponse by mutableStateOf<SignUpResponse>(Response.None)
        private set
    var sendEmailVerificationResponse by mutableStateOf<SendEmailVerificationResponse>(Response.None)
        private set

    fun sendEmailVerification() = viewModelScope.launch {
        sendEmailVerificationResponse = Response.Loading
        sendEmailVerificationResponse = authRepo.sendEmailVerification()
    }

    fun registerAndCreateUser(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        userType: UserType,
        localImageUri: String,
        car: Car = Car()
    ) = viewModelScope.launch {
        try {
            signUpResponse = Response.Loading
            when (val response =
                authRepo.firebaseSignUpWithEmailAndPasswordAsync(email, password)) {
                is Response.Failure -> Unit
                is Response.Loading -> Unit
                is Response.Success -> {
                    Log.i(LOG_TAG, "uploading photo to firebase storage")
                    val imageUrl =
                        uploadPhotoToFirebaseStorageAsync(Uri.parse(localImageUri)).await()

                    val user = when (userType) {
                        UserType.Passenger -> Passenger(
                            response.data.user!!.uid,
                            email,
                            firstName,
                            lastName,
                            phone,
                            imageUrl,
                            userType,
                            0.0,
                            0.0,
                            0
                        )

                        UserType.Driver -> Driver(
                            response.data!!.user!!.uid,
                            email,
                            firstName,
                            lastName,
                            phone,
                            imageUrl,
                            userType,
                            0.0,
                            0.0,
                            0,
                            0,
                            0,
                            car
                        )
                    }

                    userRepo.createUser(user)
                    signUpResponse = response
                }

                is Response.None -> Unit
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.toString())
        }
    }

    private suspend fun uploadPhotoToFirebaseStorageAsync(uri: Uri) = viewModelScope.async {
        val photoRef = Firebase.storage.reference.child("photos/${uri.lastPathSegment}")

        val uploadTask = photoRef.putFile(uri).await()

        return@async uploadTask.uploadSessionUri.toString()
    }
}
