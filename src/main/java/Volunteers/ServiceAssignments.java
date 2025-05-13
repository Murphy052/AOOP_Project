package Volunteers;

import java.util.HashMap;
import java.util.Map;

public class ServiceAssignments {
    private static final HashMap<String, String> _assignments = new HashMap<>();

    public static synchronized String getById(String id) {
        return _assignments.get(id);
    }

    public static synchronized void update(Map<String,String> newAssignments) {
        _assignments.clear();
        _assignments.putAll(newAssignments);
    }
}
