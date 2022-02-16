# search-manager

made with Kotlin

## Build

```sh
./gradlew build
```

## Run

```sh
java -jar build/libs/searchmanager-1.0.jar
```

## Docker

```sh
docker build . -t searchmanager
docker run -it -p 8080:8080 searchmanager
```

## Perform Search

```sh
curl 127.0.0.1:8080/search?q=your+query+here
```

