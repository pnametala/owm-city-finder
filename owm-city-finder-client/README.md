# City Finder REST Client

A convenience client that allows you to access the City Finder REST API using Kotlin.

## Using In Your Project

Add the following to your Gradle script:

```gradle
repositories {
    maven { setUrl("https://dl.bintray.com/mvysny/gitlab") }
}
dependencies {
    compile("com.gitlab.mvysny.owmcityfinder:owm-city-finder-client:0.2")
}
```

Then, start the City Finder server (the easiest way is to use Docker, see
[owm-city-finder-server](../owm-city-finder-server) for more information).

Then, just add the following Kotlin code:

```kotlin
val client = CityFinderClient("http://localhost:25314")
println(client.findByName("Helsinki"))
```

will print

```
[City(id=658226, name=Helsinki, country=FI, coord=Coord(lon=24.93417, lat=60.17556)), City(id=658225, name=Helsinki, country=FI, coord=Coord(lon=24.93545, lat=60.169521)), City(id=658224, name=Helsinki, country=FI, coord=Coord(lon=21.438101, lat=60.60778))]
```
