# TODO Applications with Quarkus

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/cescoffier/quarkus-todo-app/Build)

## Build and Run

```bash
./mvnw package
podman build $VARIANT -f src/main/docker/Dockerfile.jvm -t quarkus-todo:latest
podman-compose up # start services in Podman
podman-compose down # stop and clean up services in Podman
```

Where `$VARIANT` is `quarkus-todo` or `quarkus-todo-reactive`.

## Imperative Application

```bash
cd quarkus-todo
mvn compile quarkus:dev
```

Open: http://localhost:8080/

## Reactive Application

This version uses Hibernate Reactive, RESTEasy Reactive and Mutiny.

```bash
cd quarkus-todo-reactive
mvn compile quarkus:dev
```

Open: http://localhost:8080/

