import Server.Responses.NotFound;
import Server.Responses.Ok;
import Server.Responses.Response;
import Server.WebsocketMulticast;
import Server.types.MethodRoutePair;
import Volunteers.ServiceAssignments;
import Volunteers.VolunteerPreferences;
import Volunteers.VolunteerServices;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Router {
    private static final Server.types.Endpoints _endpoints = initEndpoints();

    public static Server.types.Endpoints getEndpoints() {
        return _endpoints;
    }

    private static Server.types.Endpoints initEndpoints() {
        Server.types.Endpoints _endpoints = new Server.types.Endpoints();

        // GET / -> Ok (for testing)
        _endpoints.put(new MethodRoutePair("GET", "/"), (request) -> new Ok());

        // GET /services -> returns {"services": [{"id": "S1", "name": "Service Name"}, ...]}
        _endpoints.put(new MethodRoutePair("GET", "/services"), (request) -> {
            JSONArray servicesArray = new JSONArray();
            VolunteerServices.getServices().forEach((key, value) -> {
                JSONObject serviceObj = new JSONObject();
                serviceObj.put("id", key);
                serviceObj.put("name", value);
                servicesArray.put(serviceObj);
            });

            JSONObject data = new JSONObject();
            data.put("service", servicesArray);

            return new Response(200, "OK", data);
        });

        // TODO: add Assignments Optimization at background + Broadcast
        // POST /preferences -> Ok
        _endpoints.put(new MethodRoutePair("POST", "/preferences"), (request) -> {
            try {
                JSONObject data = request.getBodyAsJSONObject();

                ArrayList<String> preferences = new ArrayList<>();
                data.getJSONArray("preferences").forEach(value -> {
                    preferences.add(value.toString());
                });
                VolunteerPreferences.put(data.get("volunteerId").toString(), preferences);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return new Response(400, "Bad Request");
            }

            return new Ok();
        });

        _endpoints.put(new MethodRoutePair("GET", "/broadcast"), (request) -> {
            try {
                System.out.println("Sending Hello to users");
                WebsocketMulticast.notifyAll("Hello");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return new Ok();
        });

        // GET /assignments?volunteerId={?} → returns {"assignment":"S2"} or error
        _endpoints.put(new MethodRoutePair("GET", "/assignments"), (request) -> {
            String volunteerId = request.parameters.get("volunteerId");

            if (volunteerId == null) {
                return new Response(400, "Bad Request");
            }

            String assignmentId = ServiceAssignments.getById(volunteerId);

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
