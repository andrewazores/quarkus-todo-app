/// usr/bin/env jbang "$0" "$@" ; exit $?

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//DEPS com.google.code.gson:gson:2.8.9
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SimulateLoad {

    static final Gson gson = new Gson();
    static final HttpClient http = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
    static BlockingQueue<Integer> ids;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Expected 2 arguments: server URL and request count");
        }
        String url = args[0];
        int count = Integer.parseInt(args[1]);
        ids = new ArrayBlockingQueue<>(count);

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < count; i++) {
            executor.submit(
                    () -> {
                        try {
                            createTodo(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
        for (int i = count; i > 0; i--) {
            executor.submit(
                    () -> {
                        try {
                            deleteTodo(url, ids.take());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        executor.shutdown();
    }

    static void createTodo(String url) throws Exception {
        String body =
                gson.toJson(
                        Map.of(
                                "title", UUID.randomUUID(),
                                "order", "0",
                                "completed", "false"));
        HttpRequest req =
                HttpRequest.newBuilder()
                        .uri(URI.create(String.format("%s/api", url)))
                        .setHeader("Content-Type", "application/json")
                        .POST(BodyPublishers.ofString(body))
                        .build();
        HttpResponse<String> response = http.send(req, BodyHandlers.ofString());
        String responseBody = response.body();
        try {
            Map<String, String> result =
                    gson.fromJson(responseBody, new TypeToken<Map<String, String>>() {}.getType());
            synchronized (ids) {
                ids.offer(Integer.parseInt(result.get("id")));
            }
        } catch (Exception e) {
            System.out.println(responseBody);
            e.printStackTrace();
        }
    }

    static void deleteTodo(String url, int id) throws Exception {
        HttpRequest req =
                HttpRequest.newBuilder()
                        .uri(URI.create(String.format("%s/api/%d", url, id)))
                        .setHeader("Content-Type", "application/json")
                        .DELETE()
                        .build();
        http.send(req, BodyHandlers.ofString());
    }
}
