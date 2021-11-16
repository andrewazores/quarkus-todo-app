package io.quarkus.sample;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.quarkus.panache.common.Sort;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Registered;


@Path("/api")
@Produces("application/json")
@Consumes("application/json")
public class TodoResource {

    static {
        FlightRecorder.register(TodoResourceEvent.class);
    }

    @Inject TodoLogger logger;

    @OPTIONS
    public Response opt() {
        TodoResourceEvent evt = new TodoResourceEvent("OPTIONS", -1);
        try {
            evt.begin();
            return Response.ok().build();
        } finally {
            if (evt.shouldCommit()) {
                evt.commit();
            }
        }
    }

    @GET
    public List<Todo> getAll() {
        TodoResourceEvent evt = new TodoResourceEvent("GET", -1);
        try {
            evt.begin();
            return Todo.listAll(Sort.by("order"));
        } finally {
            if (evt.shouldCommit()) {
                evt.commit();
            }
        }
    }

    @GET
    @Path("/{id}")
    public Todo getOne(@PathParam("id") Long id) {
        TodoResourceEvent evt = new TodoResourceEvent("GET", id);
        try {
            evt.begin();
            Optional<Todo> entity = Todo.findByIdOptional(id);
            return entity.orElseThrow(() -> new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND));
        } finally {
            if (evt.shouldCommit()) {
                evt.commit();
            }
        }
    }

    @POST
    @Transactional
    public Response create(@Valid Todo item) {
        TodoResourceEvent evt = new TodoResourceEvent("POST");
        try {
            evt.begin();
            logger.log("Creating TODO: " + item.title);
            boolean alreadyExists = false;
            for (Todo todo : Todo.<Todo>listAll()) {
                if (Objects.equals(item.id, todo.id)) {
                    alreadyExists = alreadyExists || true;
                }
            }
            if (alreadyExists) {
                throw new WebApplicationException(400);
            }
            item.persist();
            evt.id = item.id;
            logger.log(String.format("Created TODO<%d>: %s", item.id, item.title));
            return Response.status(Status.CREATED).entity(item).build();
        } finally {
            if (evt.shouldCommit()) {
                evt.commit();
            }
        }
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@Valid Todo todo, @PathParam("id") Long id) {
        TodoResourceEvent evt = new TodoResourceEvent("PATCH", id);
        try {
            evt.begin();
            Todo entity = Todo.findById(id);
            entity.id = id;
            entity.completed = todo.completed;
            entity.order = todo.order;
            entity.title = todo.title;
            entity.url = todo.url;
            return Response.ok(entity).build();
        } finally {
            if (evt.shouldCommit()) {
                evt.commit();
            }
        }
    }

    @DELETE
    @Transactional
    public Response deleteCompleted() {
        TodoResourceEvent evt = new TodoResourceEvent("DELETE");
        try {
            evt.begin();
            Todo.deleteCompleted();
            return Response.noContent().build();
        } finally {
            if (evt.shouldCommit()) {
                evt.commit();
            }
        }
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response deleteOne(@PathParam("id") Long id) {
        TodoResourceEvent evt = new TodoResourceEvent("DELETE", id);
        try {
            evt.begin();
            logger.log("Deleting ID: " + id);
            for (Todo todo : Todo.<Todo>listAll()) {
                if (Objects.equals(id, todo.id)) {
                    todo.delete();
                    todo.flush();
                    logger.log("Deleted ID: " + id);
                }
            }
            return Response.noContent().build();
            // Optional<Todo> entity = Todo.findByIdOptional(id);
            // entity.ifPresent(Todo::delete);
            // return entity.map(todo -> Response.noContent().build()).orElseThrow(() -> new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND));
        } finally {
            if (evt.shouldCommit()) {
                evt.commit();
            }
        }
    }

    @Name("io.quarkus.sample.TodoResource")
    @Label("Todo Resource API Event")
    @Registered(true)
    static class TodoResourceEvent extends Event {
        final String method;
        long id;

        TodoResourceEvent(String method) {
            this.method = method;
        }

        TodoResourceEvent(String method, long id) {
            this.method = method;
            this.id = id;
        }
    }

}
