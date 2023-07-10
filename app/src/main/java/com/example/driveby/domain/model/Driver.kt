package com.example.driveby.domain.model

class Driver(
    override val id: String = "",
    override val email: String = "",
    override val name: String = "",
    override val lastName: String = "",
    override val phoneNumber: String = "",
    override val imageUrl: String = "",
    override val userType: UserType = UserType.Driver,
    override val latitude: Double = 0.0,
    override val longitude: Double = 0.0,
    override var score: Int = 0,
    val ratingsCount: Int = 0,
    val ratingsSum: Int = 0,
    val car: Car = Car(),
    val drive: Drive = Drive()
) : IUser
