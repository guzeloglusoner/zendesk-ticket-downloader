# Zendesk Ticket Download

The aim of the project is to stream ZenDesk Tickets in real-time with cursor based pagination.

## Getting Started

To build such system, Play Framework 2.6 is used. Play WS and Akka-Streams combined.

### Prerequisites

To run the program, a sbt shell is needed. Intellij has all premise installation requirements.

### Logic

### Sample Call

Request
```
localhost:9000/tickets?timestamp={start-time}
```

Response
```
Ok("Download started.)
```

## Built With

* [SBT](https://maven.apache.org/) - Dependency Management


## Versioning

v0.1

## Authors

* **Soner Guzeloglu** - *Initial work*



