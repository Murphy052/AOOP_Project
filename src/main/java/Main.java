import Server.Responses.NotFound;
import Server.Responses.Response;
import Server.Responses.Ok;
import Server.Server;
import Server.types.Endpoints;
import Server.types.MethodRoutePair;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;


public class Main {
    private static final Services _services = new Services();
    private static final Endpoints _endpoints = initEndpoints();
    private static HashMap<String, ArrayList<String>> _preferences = new HashMap<>();
    private static HashMap<String, String> _assignments = new HashMap<>();

    public static void main(String[] args) {
        try {
            Server server = Server.builder()
                    .port(4221)
                    .threads(4)
                    .addEndpoints(_endpoints).build();

            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Endpoints initEndpoints() {
        Endpoints _endpoints = new Endpoints();

        // GET /
        _endpoints.put(new MethodRoutePair("GET", "/"), (request) -> new Ok());

        _endpoints.put(new MethodRoutePair("GET", "/services"), (request) -> {
            JSONArray servicesArray = new JSONArray();
            _services.getServices().forEach((key, value) -> {
                JSONObject serviceObj = new JSONObject();
                serviceObj.put("id", key);
                serviceObj.put("name", value);
                servicesArray.put(serviceObj);
            });

            JSONObject data = new JSONObject();
            data.put("service", servicesArray);

            return new Response(200, "OK", data);
        });

        // POST /preferences
        _endpoints.put(new MethodRoutePair("POST", "/preferences"), (request) -> {
            try {
                JSONObject data = request.getBodyAsJSONObject();

                ArrayList<String> preferences = new ArrayList<>();
                data.getJSONArray("preferences").forEach(value -> {
                    preferences.add(value.toString());
                });
                _preferences.put(data.get("volunteerId").toString(), preferences);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return new Response(400, "Bad Request");
            }

            System.out.println(_preferences);
            return new Ok();
        });

        // GET /assignments?volunteerId={?}
        _endpoints.put(new MethodRoutePair("GET", "/assignments"), (request) -> {
            String volunteerId = request.parameters.get("volunteerId");

            if (volunteerId == null) {
                return new Response(400, "Bad Request");
            }

            String assignmentId = _assignments.get(volunteerId);
            if (assignmentId == null) {
                return new NotFound();
            }

            JSONObject data = new JSONObject();
            data.put("assignment", assignmentId);

            return new Response(200, "OK", data);
        });

        return _endpoints;
    }
}
