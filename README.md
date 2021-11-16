# TODO Applications with Quarkus

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/cescoffier/quarkus-todo-app/Build)

## Build

```bash
./mvnw package
podman build . -t quarkus-todo:latest
```

## Run with Podman Compose

```bash
podman-compose up # start services in Podman
# TODO get JDP working so custom target isn't needed
https --form POST :8181/api/v2/targets connectUrl=service:jmx:rmi:///jndi/rmi://quarkus-todo-backend:9999/jmxrmi alias=quarkus
https --multipart :8181/api/v1/templates template@QuarkusTodoProfiling.jfc
podman-compose down # stop and clean up services in Podman
```


## Run in Dev mode

```bash
podman-compose -f database-compose.yml run --rm quarkus-todo-db
# in another terminal
cd quarkus-todo
mvn compile quarkus:dev
```

## Simulate Load

```java
jbang SimulateLoad.java http://localhost:8080 10
```
