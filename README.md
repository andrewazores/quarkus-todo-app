# TODO Applications with Quarkus

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/cescoffier/quarkus-todo-app/Build)

## Build and Run

```bash
./mvnw package
podman build $VARIANT -f src/main/docker/Dockerfile.jvm -t quarkus-todo:latest
podman-compose up # start services in Podman
# TODO get JDP working so custom target isn't needed
https --form POST :8181/api/v2/targets connectUrl=service:jmx:rmi:///jndi/rmi://quarkus-todo-backend:9999/jmxrmi alias=quarkus
podman-compose down # stop and clean up services in Podman
```

Where `$VARIANT` is `quarkus-todo` or `quarkus-todo-reactive`.

```java
jbang SimulateLoad.java http://localhost:8080 10
```

## Imperative Application

```bash
podman-compose -f database-compose.yml run --rm quarkus-todo-db
# in another terminal
cd quarkus-todo
mvn compile quarkus:dev
```

Open: http://localhost:8080/

## Reactive Application

This version uses Hibernate Reactive, RESTEasy Reactive and Mutiny.

```bash
podman-compose -f database-compose.yml run --rm quarkus-todo-db
# in another terminal
cd quarkus-todo-reactive
mvn compile quarkus:dev
```

Open: http://localhost:8080/

