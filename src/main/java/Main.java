import Server.Responses.Response;
import Server.Responses.Ok;
import Server.Server;
import Server.types.Endpoints;
import Server.types.MethodRoutePair;

import java.util.ArrayList;
import java.util.HashMap;


public class Main {
    public static void main(String[] args) {
        HashMap<String, String> services = new HashMap<>();
        HashMap<String, String> preferences = new HashMap<>();
        HashMap<String, String> assignments = new HashMap<>();

        services.put("S1", "Food Distribution");
        services.put("S2", "Medical Aid");
        services.put("S3", "Shelter Setup");

        Endpoints endpoints = new Endpoints();

        endpoints.put(new MethodRoutePair("GET", "/"), (request) -> new Ok());

        endpoints.put(new MethodRoutePair("GET", "/body"), (request) -> {
            String body = "Hello";
            return new Response(200, "OK", body);
        });

        endpoints.put(new MethodRoutePair("POST", "/post"), (request) -> {
            String body = request.body;
            if (body == null) return new Response(400, "Bad Server.Request");
            return new Response(200, "OK", body);
        });


        try {
            Server server = Server.builder()
                    .port(4221)
                    .threads(4)
                    .addEndpoints(endpoints).build();

            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Response root() {
        return new Ok();
    }
}
