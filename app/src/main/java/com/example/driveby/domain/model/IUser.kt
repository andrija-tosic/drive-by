package com.example.driveby.domain.model

interface IUser {
    val id: String
    val email: String
    val name: String
    val lastName: String
    val phoneNumber: String
    val imageUrl: String
    val userType: UserType
    val latitude: Double
    val longitude: Double
    val score: Int
}
