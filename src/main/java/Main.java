import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Server.Responses.Response;
import Server.Responses.Ok;
import Server.Server;
import Server.types.Endpoints;
import Server.types.MethodRoutePair;

public class Main {
  /**
   * Assigns exactly one service to each volunteer (or null if no slot),
   * minimizing the sum of preference ranks using the Hungarian algorithm.
   */
  public static Map<String, String> optimizeHungarian(
      Map<String, List<String>> prefs,
      List<String> services) {
    List<String> volunteers = new ArrayList<>(prefs.keySet());
    int vCount = volunteers.size();
    int sCount = services.size();
    int n = Math.max(vCount, sCount);

    // Pad services so we have a square cost matrix.
    List<String> paddedServices = new ArrayList<>(services);
    for (int i = sCount; i < n; i++) {
      paddedServices.add(null);
    }

    // Build cost matrix
    int penalty = sCount + 1;
    int[][] cost = new int[n][n];
    for (int i = 0; i < n; i++) {
      List<String> prefList = i < vCount
          ? prefs.getOrDefault(volunteers.get(i), Collections.emptyList())
          : Collections.emptyList();
      for (int j = 0; j < n; j++) {
        String svc = paddedServices.get(j);
        int idx = prefList.indexOf(svc);
        cost[i][j] = (idx >= 0) ? idx : penalty;
      }
    }

    // Run Hungarian
    Hungarian hung = new Hungarian(cost);
    int[] rowToCol = hung.execute();

    // Build result map
    Map<String, String> assign = new HashMap<>();
    for (int i = 0; i < vCount; i++) {
      int col = rowToCol[i];
      String svc = (col < sCount) ? services.get(col) : null;
      assign.put(volunteers.get(i), svc);
    }
    return assign;
  }

  // Hungarian (Munkres) implementation
  static class Hungarian {
    private final int n;
    private final int[][] cost;
    private final int[] u, v, p, way;

    public Hungarian(int[][] cost) {
      this.n    = cost.length;
      this.cost = cost;
      this.u = new int[n+1];
      this.v = new int[n+1];
      this.p = new int[n+1];
      this.way = new int[n+1];
    }

    public int[] execute() {
      for (int i = 1; i <= n; i++) {
        p[0] = i;
        int j0 = 0;
        int[] minv = new int[n+1];
        boolean[] used = new boolean[n+1];
        Arrays.fill(minv, Integer.MAX_VALUE);
        // used defaults to false

        do {
          used[j0] = true;
          int i0 = p[j0], delta = Integer.MAX_VALUE, j1 = 0;
          for (int j = 1; j <= n; j++) {
            if (used[j]) continue;
            int cur = cost[i0-1][j-1] - u[i0] - v[j];
            if (cur < minv[j]) { minv[j] = cur; way[j] = j0; }
            if (minv[j] < delta) { delta = minv[j]; j1 = j; }
          }
          for (int j = 0; j <= n; j++) {
            if (used[j]) { u[p[j]] += delta; v[j] -= delta; }
            else { minv[j] -= delta; }
          }
          j0 = j1;
        } while (p[j0] != 0);

        do {
          int j1 = way[j0];
          p[j0] = p[j1];
          j0 = j1;
        } while (j0 != 0);
      }

      int[] rowToCol = new int[n];
      for (int j = 1; j <= n; j++) {
        if (p[j] > 0 && p[j] <= n) {
          rowToCol[p[j]-1] = j-1;
        }
      }
      return rowToCol;
    }
  }

  public static void main(String[] args) {
    // --- placeholder data ---
    Map<String, String> _services = new HashMap<>();
    _services.put("S1", "Food Distribution");
    _services.put("S2", "Medical Aid");
    _services.put("S3", "Shelter Setup");

    // Now declared to match optimizeHungarian signature
    Map<String, List<String>> _preferences = new HashMap<>();
    _preferences.put("V1", Arrays.asList("S1","S2","S3"));
    _preferences.put("V2", Arrays.asList("S2","S3","S1"));
    _preferences.put("V3", Arrays.asList("S3","S1","S2"));

    // store assignments here
    Map<String, String> _assignments = new HashMap<>();

    // --- build endpoints ---
    Endpoints endpoints = new Endpoints();

    // health check
    endpoints.put(new MethodRoutePair("GET", "/"), (request) -> new Ok());

    // test POST
    endpoints.put(new MethodRoutePair("POST", "/post"), (request) -> {
      if (request.body == null) {
        return new Response(400, "Bad Request");
      }
      return new Response(200, "OK", request.body);
    });

    // GET /optimize → runs Hungarian, updates _assignments, returns JSON
    endpoints.put(new MethodRoutePair("GET", "/optimize"), (request) -> {
      List<String> svcIds = new ArrayList<>(_services.keySet());
      Map<String,String> result = optimizeHungarian(_preferences, svcIds);
      _assignments.clear();
      _assignments.putAll(result);

      // build JSON: {"assignments":{ "V1":"S1", ... }}
      StringBuilder sb = new StringBuilder("{\"assignments\":{");
      boolean first = true;
      for (Map.Entry<String,String> e : result.entrySet()) {
        if (!first) sb.append(",");
        sb.append("\"").append(e.getKey()).append("\":");
        sb.append(e.getValue()==null ? "null" : "\"" + e.getValue() + "\"");
        first = false;
      }
      sb.append("}}");
      return new Response(200, "OK", sb.toString());
    });

    // GET /assignment?volunteerId=V2 → returns {"assignment":"S2"} or error
    endpoints.put(new MethodRoutePair("GET", "/assignment"), (request) -> {
      String vid = request.parameters.get("volunteerId");
      String svc = _assignments.get(vid);
      String body;
      if (svc != null) {
        body = "{\"assignment\":\"" + svc + "\"}";
      } else {
        body = "{\"error\":\"No assignment for " + vid + "\"}";
      }
      return new Response(200, "OK", body);
    });

    // start server
    try {
      Server server = Server.builder()
          .port(4221)
          .threads(4)
          .addEndpoints(endpoints)
          .build();
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
