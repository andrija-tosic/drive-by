package com.example.driveby.presentation.sign_up

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveby.core.Constants.LOG_TAG
import com.example.driveby.domain.model.Response
import com.example.driveby.domain.model.User
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
    val authRepo: AuthRepository,
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
    ) = viewModelScope.launch {
        try {
            signUpResponse = Response.Loading
            val response = authRepo.firebaseSignUpWithEmailAndPasswordAsync(email, password)
            when (response) {
                is Response.Failure -> Unit
                is Response.Loading -> Unit
                is Response.Success -> {
                    Log.i(LOG_TAG, "uploading photo to firebase storage")
                    val imageUrl =
                        uploadPhotoToFirebaseStorageAsync(Uri.parse(localImageUri)).await()

                    Log.i(LOG_TAG, "creating user")
                    val user = User(
                        response.data!!.user!!.uid,
                        email,
                        firstName,
                        lastName,
                        phone,
                        imageUrl,
                        userType
                    )
                    Log.i(LOG_TAG, "created user $user")

                    Log.i(LOG_TAG, "repo creating $user")

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
