Games Explorer
------

**Provide stateless services for games**

## Overview

- Score

*Service to manage hight score*

- Matchmaking

*Service to create automatic lobbies to regroup players*

- Lobbies

*Service to manage multiple lobbies*

- UDP Hole Punching

*Service to transmit IP address and port across NAT without UPnP*

## How start

### Configuration

Use environement variable to configure the server



- Startup

**holepunching** To start Hole Punching service

**lobies** To start Lobies service
**score** To start Score service

**matchmaking** To start the Matchmaking service

**webapi** To start the Web Proxy. **Require if you are not using Edge API in your hazelcast cluster**



- Database

**MONGODB_ADDON_HOST** Hostname of your mongo database. Default 127.0.0.1

**MONGODB_ADDON_PORT** Port of your mongo database. Default 27017

**MONGODB_ADDON_DB** Database name of used. Default *edge_games_scores*

**MONGODB_ADDON_USER** Username of your mongo database

**MONGODB_ADDON_PASSWORD** Password of your mongo database

-- Hole Punching

**HOLEPUNCHING_PORT** UDP Port use for Hole Punching. Default 34000

**HOLEPUNCHING_INTERFACE** Interface bind for Hole Punching. All by default (0.0.0.0)

-- Matchmaking

**MIN_PLAYERS_PER_SESSION** Number of player required to start session. Default 2

**MAX_PLAYERS_PER_SESSION** Number of maximum players in a session. Default 4

**WAIT_START_SESSION_TIME** Time in seconde to wait other players when a session is ready to start with the minimum of player. Default 15

### Run on promise

Build and package to create a standalone jar

```
mvn package 
```

Start the jar
```
java -jar target/games-explorer-<version>.jar 
```

## Cloud ready

### [Clever-Cloud](https://www.clever-cloud.com/)

Push the sources with git to your Clever-Cloud application

## Architecture

**Games Explorer use [vert.x](https://vertx.io/) and [ReactiveX](http://reactivex.io/) to build reactive and asynchronous services**

All REST API are mount behind Edge Web Proxy (embedded in this project)
OpenAPI ressources are store in src/main/resources