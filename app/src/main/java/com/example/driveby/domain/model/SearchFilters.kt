package com.example.driveby.domain.model

class SearchFilters {
    var radius: Int = 1000
    var seats: Int = 1000
    var stars: Int = 5

    constructor(radius: Int, seats: Int, stars: Int) {
        this.radius = radius
        this.seats = seats
        this.stars = stars
    }

    constructor()
}
