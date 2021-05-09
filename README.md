# Learn Tool

## Introduction
The Learn Tool application is a web application that aims to improve the study and learning process.
Learn Tool consists mainly of an active recall learning tool, but offers a whole range of functionalities, from importing a dataset to creating custom cards and workspaces.

## Development

Learn Tool is a web application that uses Spring Boot 2 framework and Kotlin language on the backend, and Angular framework on the frontend.

### Setup

Install OpenJDK 11 and Docker (Docker compose).

### Build backend project (development)

#### Build backend and run unit tests

```sh
cd ./backend
./gradlew clean build
```

#### Build backend and run unit tests and integration tests

```sh
cd ./backend
./gradlew clean build integrationTest
```

### Build frontend project (development)

```sh
cd ./frontend
mvn clean build
```

### Build project (production)

#### Build backend and run unit tests

```sh
./gradlew clean build
```

#### Build backend and run unit tests and integration tests

```sh
./gradlew clean build integrationTest
```

### Klint

Check:

```sh
./gradlew ktlintCheck
```

Format:

```sh
./gradlew ktlintFormat
```

### Create Docker images

```sh
./gradlew jibDockerBuild
```

### Setup Docker compose variables

Create a file .env with the following content:

```sh
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=postgres
```

### Run application (development)

Run the backend application:

```sh
docker-compose up
```

Run the frontend application:

```sh
cd ./frontend
npm start
```

### Run application (production)

```sh
docker-compose up
```

### Access PostgreSQL

```sh
docker exec -it learn-tool_db_1 /bin/bash
psql learntool $POSTGRES_USERNAME
```

### Access application

```sh
http://localhost:8080
```

## REST APIs documentation

The folder "documents" contains the OpenAPI REST APIs documentation.
The documentation is compliant with Swagger UI and Postman.

Run the following command to validate the OpenAPI documentation file:
 
```sh
./gradlew validateOpenApi
```
