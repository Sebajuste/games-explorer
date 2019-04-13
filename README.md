Games Explorer
------

**Provide stateless services for games**

# Overview

### Score

Service to mange hight score

### Matchmaking

Service to create automatic lobbies to regroup players

### Lobbies

Service to manage multiple lobbies

### UDP Hole Punching

Service to transmit IP address and port across NAT without UPnP

# Architecture

**Games Explorer use vert.x and ReactiveX to build reactive and asynchronous services**

All REST API are mount behind Edge Web Proxy (embedded in this project)
