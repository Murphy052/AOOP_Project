package Volunteers;

import java.util.HashMap;

public class VolunteerServices {
    private static final HashMap<String, String> _services = initServices();

    private static HashMap<String, String> initServices() {
        HashMap<String, String> services = new HashMap<>();
        services.put("S1", "Food Distribution");
        services.put("S2", "Medical Aid");
        services.put("S3", "Shelter Setup");

        return services;
    }

    public static HashMap<String, String> getServices() {return _services;}
}
