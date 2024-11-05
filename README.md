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

[Docker](https://www.docker.com/) is used to run the applications and host the databases. Please
download Docker and sign in before executing the steps below.

One docker image is built that contains all three applications, but, each application will have its
own running container. There is also a postgres container for production, and for local testing a testing database.

## Set up

1. Make sure [Docker](https://www.docker.com/) is installed and running on your machine. Remember to log in!

1.  Clean-up all Docker containers and data. Starting with a clean slate is good!
    ```bash
    docker-compose down --volumes 
    ```

1.  Build the applications to place into the Docker container.
    ```bash
    ./gradlew clean build -x test
    ```

1. Build the Docker container.
    ```bash
    docker build . --file Dockerfile --tag collision-mapper
    ```

1.  Run docker-compose. This will start all three applications, the databases needed for producation/testing, and 
perform the needed migrations. It is very important that you wait at least a minute for all of the applications to start
running before starting the system tests.
    ```bash
    docker-compose up
    ```
    The applications need to wait for the database container to issue a health report before starting. This takes about 
30 seconds, please be patient before attempting to access the data pages.

1. PLEASE WAIT TO RUN THIS, THE DATABASE MUST BE READY. Check the Docker logs and verify the database is accepting
connections. Runs the system tests.
    ```bash
    ./gradlew build
    ```

1. Verification of data storage. Open a web browser to
    ```
     http://127.0.0.1:8886/view-data
    ```
   Observe the list of stored elements! If nothing appears please wait a minute or so and refresh the page. Check the 
Docker logs and make sure that the collector/analyzer have finished processing their data batches.

1. To view the collision map go to the url below. The red circles are collision hotspots. Click on one to see the number
of collisions in that area! Zoom in and out to see the collisions change depending on your view.
    ```
     http://127.0.0.1:8888/
    ```

1. To view the server metrics go to the curl below. Be sure to execute some queries in the search bar. By default, nothing is displayed!
In the target tab you will see the servers being monitored. In the graph tab you can view the scraped data, just make sure
to execute a query in the search bar! A good thing to query is `requests`, which shows the number of requests made to the
`basic-server` API that returns the circle locations. Type `requests` into the search bar and press execute to see the data!
Trying panning around the screen in the map for a while and see it affect the returned values.
    ```
     http://127.0.0.1:9090/
    ```

1.  Clean-up all Docker containers and data.
    ```bash
    docker-compose down --volumes 
    ```
   
The End!
