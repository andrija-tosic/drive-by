package com.example.driveby.domain.model

class Passenger(
    override val id: String = "",
    override val email: String = "",
    override val name: String = "",
    override val lastName: String = "",
    override val phoneNumber: String = "",
    override val imageUrl: String = "",
    override val userType: UserType = UserType.Passenger,
    override val latitude: Double = 0.0,
    override val longitude: Double = 0.0,
    override var score: Int = 0,
) : IUser {

}