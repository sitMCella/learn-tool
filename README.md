# Learn Tool

![Cards](https://github.com/sitMCella/learn-tool/wiki/images/Cards1.png)

## Table of contents

* [Introduction](#introduction)
* [Configuration](#configuration)
* [Development](#development)
  * [Setup](#setup)
  * [Build project](#build-project)
  * [Backend Linter](#backend-linter)
  * [Frontend Linter](#frontend-linter)
  * [Create Docker images](#create-docker-images)
  * [Setup Docker compose variables](#setup-docker-compose-variables)
  * [Run application](#run-application)
  * [Access application](#access-application)
  * [Backup the database](#backup-the-database)
* [REST APIs documentation](#rest-apis-documentation)

## Introduction

Learn Tool is a web application that aims to improve the study and learning process.
Learn Tool consists of an active recall learning tool, and offers a whole range of functionalities, from importing a dataset to create custom cards and workspaces.

Read the [documentation](https://github.com/sitMCella/learn-tool/wiki) in order to discover the features of Learn Tool.

## Configuration

Learn Tool application uses Oauth 2.0 and JWT authentication. Learn Tool supports the local authentication and the Google authentication.
Read the page [google_authentication](google_authentication.md) in order to configure Google Cloud Platform.

## Development

Learn Tool is a web application built using Spring Boot 2 framework and Kotlin language on the backend, and React JS framework on the frontend.

### Setup

Install OpenJDK 11, Node.js >= 12.4.0, and Docker (Docker compose).

### Build project

#### Build backend (development) and run unit tests

```sh
./gradlew clean build -PskipWebApp
```

#### Build backend (development) and run unit tests and integration tests

```sh
./gradlew clean build integrationTest -PskipWebApp
```

#### Build project (production) and run unit tests

```sh
./gradlew clean build
```

#### Build project (production) and run unit tests and integration tests

```sh
./gradlew clean build integrationTest
```

### Backend Linter

#### Klint Check:

```sh
./gradlew ktlintCheck
```

#### Klint Format:

```sh
./gradlew ktlintFormat
```

### Frontend Linter

#### Eslint Check:

```sh
cd ./frontend
eslint --ext .jsx,.js src/
```

#### Eslint Format:

```sh
cd ./frontend
eslint --fix --ext .jsx,.js src/
```

### Create Docker images

#### Create Docker images

```sh
./gradlew jibDockerBuild
```

### Setup Docker compose variables

Create a file .env with the following content:

```sh
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=postgres
```

### Run application

#### Run the backend application (development)

```sh
docker-compose -f docker-compose-development.yml up
```

#### Run the frontend application (development)

```sh
cd ./frontend
npm start
```

#### Run application (production)

```sh
docker-compose up --build
```

### Access application

#### Access the application in development

```sh
http://localhost:3000
```

#### Access the application in production

```sh
http://localhost:80
```

#### Access PostgreSQL

```sh
docker exec -it db /bin/bash
psql learntool $POSTGRES_USERNAME
```

### Backup and Restore the database

### Backup the database

```sh
docker exec -t db pg_dumpall -c -U postgres > backup/dump_`date +%d-%m-%Y"_"%H_%M_%S`.sql
```

#### Restore the database

```sh
cat backup/<dump_file> | docker exec -i db psql -U postgres
```

## REST APIs documentation

The directory "backend/documents" contains the OpenAPI REST APIs documentation.
The documentation is compliant with Swagger UI and Postman.

Run the following command to validate the OpenAPI documentation file:
 
```sh
./gradlew validateOpenApi
```
