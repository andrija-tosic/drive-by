package com.example.driveby.domain.model

class Drive {
    var active = false
    val startLatitude = -1.0
    val startLongitude = -1.0
    var endLatitude = -1.0
    var endLongitude = -1.0
    val passengers = hashMapOf<String, Passenger>()
}