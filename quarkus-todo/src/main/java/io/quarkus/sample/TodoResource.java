package io.quarkus.sample;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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


@Path("/api")
@Produces("application/json")
@Consumes("application/json")
public class TodoResource {

    @OPTIONS
    public Response opt() {
        return Response.ok().build();
    }

    @GET
    public List<Todo> getAll() {
        return Todo.listAll(Sort.by("order"));
    }

    @GET
    @Path("/{id}")
    public Todo getOne(@PathParam("id") Long id) {
        Optional<Todo> entity = Todo.findByIdOptional(id);
        return entity.orElseThrow(() -> new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND));
    }

    @POST
    @Transactional
    public Response create(@Valid Todo item) {
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
        return Response.status(Status.CREATED).entity(item).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@Valid Todo todo, @PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        entity.id = id;
        entity.completed = todo.completed;
        entity.order = todo.order;
        entity.title = todo.title;
        entity.url = todo.url;
        return Response.ok(entity).build();
    }

    @DELETE
    @Transactional
    public Response deleteCompleted() {
        Todo.deleteCompleted();
        return Response.noContent().build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response deleteOne(@PathParam("id") Long id) {
        for (Todo todo : Todo.<Todo>listAll()) {
            if (Objects.equals(id, todo.id)) {
                todo.delete();
                todo.flush();
            }
        }
        return Response.noContent().build();
        // Optional<Todo> entity = Todo.findByIdOptional(id);
        // entity.ifPresent(Todo::delete);
        // return entity.map(todo -> Response.noContent().build()).orElseThrow(() -> new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND));
    }

}
