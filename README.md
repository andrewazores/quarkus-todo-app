# TODO Applications with Quarkus

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/cescoffier/quarkus-todo-app/Build)

Quarkus-based Todo list application in a demo setup with Cryostat for monitoring
and profiling.

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

## Run in OpenShift

### Prerequisites

Ensure that you have pushed the Todo app container image somewhere that your
OpenShift cluster can pull from. For example:

```bash
podman login quay.io
podman tag quarkus-todo:latest quay.io/myquayusername/quarkus-todo:latest
podman push quay.io/myquayusername/quarkus-todo:latest
```

Install Cryostat in your intended OpenShift project namespace using the Cryostat
Operator. Search for 'Cryostat' in the OperatorHub UI of your OpenShift admin
console, or visit:
https://cryostat.io/get-started/#installing-cryostat-operator .

### Deploy

```bash
# if postgres crash loops it probably is failing to chmod its data dir. temporary hackaround:
# oc adm policy add-scc-to-user anyuid -n $(oc project -q) -z default
oc new-app docker.io/library/postgres:13.1 POSTGRES_USER=restcrud POSTGRES_PASSWORD=restcrud POSTGRES_DB=rest-crud --name=quarkus-todo-db
oc new-app quay.io/andrewazores/quarkus-todo:latest
oc patch svc/quarkus-todo -p '{"spec":{"$setElementOrder/ports":[{"port":8080},{"port":9999}],"ports":[{"name":"jfr-jmx","port":9999}]}}'
oc expose --port 8080 svc quarkus-todo
```

### Undeploy

```bash
oc delete all -l app=quarkus-todo
$oc delete all -l app=quarkus-todo-db
```
