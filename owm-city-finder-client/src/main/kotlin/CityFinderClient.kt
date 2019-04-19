package com.gitlab.mvysny.owmcityfinder.client

data class Coord(var lon: Double, var lat: Double)
data class City(var id: Long, var name: String, var country: String, var coord: Coord)

class CityFinderClient(val baseUrl: String) {

}