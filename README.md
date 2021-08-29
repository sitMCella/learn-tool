# Learn Tool

## Introduction
The Learn Tool application is a web application that aims to improve the study and learning process.
Learn Tool consists mainly of an active recall learning tool, but offers a whole range of functionalities, from importing a dataset to creating custom cards and workspaces.

## Development

Learn Tool is a web application that uses Spring Boot 2 framework and Kotlin language on the backend, and React JS framework on the frontend.

### Setup

Install OpenJDK 11 and Docker (Docker compose).

### Build backend project (development)

#### Build backend and run unit tests

```sh
./gradlew clean build -PskipWebApp
```

#### Build backend and run unit tests and integration tests

```sh
./gradlew clean build integrationTest -PskipWebApp
```

### Build frontend project (development)

```sh
cd ./frontend
./gradlew clean build
```

### Build project (production)

#### Build project and run unit tests

```sh
./gradlew clean build
```

#### Build project and run unit tests and integration tests

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

### Create Docker images (development)

```sh
./gradlew jibDockerBuild -PskipWebApp
```

### Create Docker images (production)

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
docker-compose up --build
```

### Access PostgreSQL

```sh
docker exec -it learn-tool_db_1 /bin/bash
psql learntool $POSTGRES_USERNAME
```

### Access application

```sh
http://localhost:80
```

## REST APIs documentation

The folder "documents" contains the OpenAPI REST APIs documentation.
The documentation is compliant with Swagger UI and Postman.

Run the following command to validate the OpenAPI documentation file:
 
```sh
./gradlew validateOpenApi
```
