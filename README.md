# Learn Tool

## Introduction
The Learn Tool application is a web application that aims to improve the study and learning process.
Learn Tool consists mainly of an active recall learning tool, but offers a whole range of functionalities, from importing a dataset to creating custom cards and workspaces.

## Development

Learn Tool is a web application that uses Spring Boot 2 framework and Kotlin language on the backend, and Angular framework on the frontend.

### Setup

Install OpenJDK 11 and PostgreSQL.

### Build project

```sh
./gradlew clean build
```

### Create Docker images

```sh
./gradlew jibDockerBuild
```

### Run application

```sh
docker-compose up
```

### SSH into PostgreSQL

```sh
docker exec -it learn-tool_db_1 /bin/bash
```
