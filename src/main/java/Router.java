import Server.Responses.NotFound;
import Server.Responses.Ok;
import Server.Responses.Response;
import Server.WebsocketMulticast;
import Server.types.MethodRoutePair;
import Volunteers.Hungarian;
import Volunteers.ServiceAssignments;
import Volunteers.VolunteerPreferences;
import Volunteers.VolunteerServices;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            data.put("services", servicesArray);

            return new Response(200, "OK", data);
        });

        _endpoints.put(new MethodRoutePair("OPTIONS", "/preferences"), request -> {
            return new Response(200, "OK");
        });

        // POST /preferences -> Ok
        _endpoints.put(new MethodRoutePair("POST", "/preferences"), (request) -> {
            try {
                // Request body
                JSONObject data = request.getBodyAsJSONObject();

                // Parsing preference from request body
                ArrayList<String> preferences = new ArrayList<>();
                data.getJSONArray("preferences").forEach(value -> {
                    preferences.add(value.toString());
                });

                // Update global preferences
                VolunteerPreferences.put(data.get("volunteerId").toString(), preferences);

                // Distribute Services according to preferences
                Map<String, String> assignments = distributeServices();

                return new Response(200, "OK", assignments.get(data.get("volunteerId").toString()));
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(400, "Bad Request");
            }

//            return new Ok();
        });

        // For test
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
            System.out.println("Salam Mezahir");
            JSONObject data = new JSONObject();
            data.put("assignment", assignmentId);

            return new Response(200, "OK", data);
        });

        return _endpoints;
    }

    private static Map<String, String> distributeServices() {
        HashMap<String, ArrayList<String>> prefs = VolunteerPreferences.get();
        List<String> services = new ArrayList<>(VolunteerServices.getServices().keySet());

        Map<String, String> assignments = Hungarian.optimizeHungarian(prefs, services);
        ServiceAssignments.update(assignments);

//        new Thread(() -> { // Broadcast new assignments on background Thread
            try {
                WebsocketMulticast.notifyAll(new JSONObject(assignments));
            } catch (IOException e) {
                e.printStackTrace();
            }
//        });

        return assignments;
    }
}
