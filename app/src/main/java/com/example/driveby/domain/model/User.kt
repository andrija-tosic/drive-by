package com.example.driveby.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val imageUrl: String = "",
    val userType: UserType = UserType.Regular,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
