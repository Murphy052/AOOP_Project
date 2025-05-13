import Server.Responses.Response;
import Server.Responses.Ok;
import Server.Server;
import Server.types.Endpoints;
import Server.types.MethodRoutePair;


public class Main {
    public static void main(String[] args) {
        Endpoints endpoints = new Endpoints();

        endpoints.put(new MethodRoutePair("GET", "/"), (request) -> new Ok());

        endpoints.put(new MethodRoutePair("GET", "/body"), (request) -> {
            String body = "Hello";
            return new Response(200, "OK", body);
        });

        endpoints.put(new MethodRoutePair("GET", "/user-agent"), (request) -> {
            String body = request.headers.get("User-Agent");
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
