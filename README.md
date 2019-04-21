# City Finder: An OpenWeatherMap city resolver

[OpenWeatherMap](https://openweathermap.org) City Finder & Resolver - allows you to search a city based on its name and returns its ID and GPS, so that you can use
the [OpenWeatherMap API](https://openweathermap.org/api) to fetch weather for the city.

Useful if you need to auto-complete a city based on user input in order to display a weather forecast from the OpenWeatherMap.
This server will automatically download and index the list of cities and will provide REST endpoint which allows your Android app to search for a city. 

## Run

The easiest way is to simply run the [owm-city-finder-server Docker image](https://cloud.docker.com/repository/docker/mvysny/owm-city-finder-server):

```bash
docker run --rm -ti -p25314:25314 mvysny/owm-city-finder-server:0.2
```

To test, simply run curl:

```bash
$ curl localhost:25314/city?query=helsinki
[{"id":658226,"name":"Helsinki","country":"FI","coord":{"lon":24.93417,"lat":60.17556}},{"id":658225,"name":"Helsinki","country":"FI","coord":{"lon":24.93545,"lat":60.169521}},{"id":658224,"name":"Helsinki","country":"FI","coord":{"lon":21.438101,"lat":60.60778}}]
```

## REST API

See [owm-city-finder-server](owm-city-finder-server) for more documentation.

## Kotlin REST Client

See [owm-city-finder-client](owm-city-finder-client) for more details.
