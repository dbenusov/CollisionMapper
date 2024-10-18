# Collision Mapper

An [application continuum](https://www.appcontinuum.io/) style application using Kotlin and Ktor
that includes a single web application with two background workers.

The web server will display on a map the top 10 most dangerous intersections
based on number of accidents.

Applications:
* Basic web application
* Data analyzer
* Data collector

### Technology stack

This codebase is written in a language called [Kotlin](https://kotlinlang.org) that is able to run on the JVM with full
Java compatibility.
It uses the [Ktor](https://ktor.io) web framework, and runs on the [Netty](https://netty.io/) web server.
HTML templates are written using [Freemarker](https://freemarker.apache.org).
The codebase is tested with [JUnit](https://junit.org/) and uses [Gradle](https://gradle.org) to build a jarfile.

One docker image is built that contains all three applications, but, each application will have its
own running container. There is also a postgres container for production, and for local testing a testing database.

## Getting Started

## Set up

1.  Build the applications to place into the docker containers.
    ```shell
    ./gradlew clean build -x test
    ```

1. Build the Docker container.
    ```bash
    docker build . --file Dockerfile --tag collision-mapper
    ```

1.  Run docker-compose. This will start all three applications, the databases needed for producation/testing, and perform the needed migrations.
    ```bash
    docker-compose up
    ```

1. Run the system tests.
    ```shell
    ./gradlew test
    ```

1.  Clean-up all Docker containers and data.
    ```bash
    docker-compose down --volumes 
    ```
   
The End!
