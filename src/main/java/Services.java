import java.util.HashMap;

public class Services {
    private final HashMap<String, String> _services;

    public Services() {
        HashMap<String, String> services = new HashMap<>();
        services.put("S1", "Food Distribution");
        services.put("S2", "Medical Aid");
        services.put("S3", "Shelter Setup");

        this._services = services;
    }

    public HashMap<String, String> getServices() {return _services;}
}
