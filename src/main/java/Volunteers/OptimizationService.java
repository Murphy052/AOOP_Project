package Volunteers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OptimizationService implements Runnable {
    public void run() {
        List<String> svcIds = new ArrayList<>(VolunteerServices.getServices().keySet());
        Map<String, String> result = Hungarian.optimizeHungarian(VolunteerPreferences.get(), svcIds);
        ServiceAssignments.update(result);
    }
}
