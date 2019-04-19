# City Finder REST Server

The City Finder server which allows you to search in OpenWeatherMap cities.

## Running

The easiest way is to simply run the [owm-city-finder-server Docker image](https://cloud.docker.com/repository/docker/mvysny/owm-city-finder-server):

```bash
docker run --rm -ti -p25314:25314 mvysny/owm-city-finder-server:0.1
```

Alternatively, build the server from sources. That will build a zip file which
you can simply run for yourself:

```bash
git clone https://gitlab.com/mvysny/owm-city-finder
cd owm-city-finder
./gradlew
cd owm-city-finder-server/build/distributions
unzip *.zip
./server
```

To test the server, just run curl:

```bash
$ curl localhost:25314/city?query=helsinki
[{"id":658226,"name":"Helsinki","country":"FI","coord":{"lon":24.93417,"lat":60.17556}},{"id":658225,"name":"Helsinki","country":"FI","coord":{"lon":24.93545,"lat":60.169521}},{"id":658224,"name":"Helsinki","country":"FI","coord":{"lon":21.438101,"lat":60.60778}}]
```

## REST API

The server exposes the following REST endpoints:

* `/city/:id` - retrieves a JSON definition of a city with given ID. For example, `curl localhost:25314/city/658226` will print `{"id":658226,"name":"Helsinki","country":"FI","coord":{"lon":24.93417,"lat":60.17556}}`
* `/city?query=Hel` - finds a city by its name; supports partial name matching. For example `curl localhost:25314/city?query=helsin` will print `[{"id":658226,"name":"Helsinki","country":"FI","coord":{"lon":24.93417,"lat":60.17556}},{"id":2706766,"name":"Helsingborgs Kommun","country":"SE","coord":{"lon":12.75,"lat":56.083328}}, ...]`
