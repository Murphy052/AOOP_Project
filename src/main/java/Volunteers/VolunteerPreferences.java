package Volunteers;

import java.util.ArrayList;
import java.util.HashMap;

public class VolunteerPreferences {
    private static final HashMap<String, ArrayList<String>> _preferences = new HashMap<>();

    public static synchronized void put(String volunteerId, ArrayList<String> preferences) {
        _preferences.put(volunteerId, preferences);
    }

    public static synchronized HashMap<String, ArrayList<String>> get() {
        return _preferences;
    }
}
