package Volunteers;

import java.util.*;

// Volunteers.Hungarian (Munkres) Algorithm implementation
public class Hungarian {
    private final int n;
    private final int[][] cost;
    private final int[] u, v, p, way;

    public Hungarian(int[][] cost) {
        this.n = cost.length;
        this.cost = cost;
        this.u = new int[n + 1];
        this.v = new int[n + 1];
        this.p = new int[n + 1];
        this.way = new int[n + 1];
    }

    public int[] execute() {
        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            int[] minv = new int[n + 1];
            boolean[] used = new boolean[n + 1];
            Arrays.fill(minv, Integer.MAX_VALUE);

            do {
                used[j0] = true;
                int i0 = p[j0], delta = Integer.MAX_VALUE, j1 = 0;

                for (int j = 1; j <= n; j++) {
                    if (used[j]) continue;
                    int cur = cost[i0 - 1][j - 1] - u[i0] - v[j];
                    if (cur < minv[j]) {
                        minv[j] = cur;
                        way[j] = j0;
                    }
                    if (minv[j] < delta) {
                        delta = minv[j];
                        j1 = j;
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minv[j] -= delta;
                    }
                }

                j0 = j1;

            } while (p[j0] != 0);

            // Reconstruct matching
            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        // Build result: row (volunteer) → col (service)
        int[] rowToCol = new int[n];
        for (int j = 1; j <= n; j++) {
            if (p[j] > 0 && p[j] <= n) {
                rowToCol[p[j] - 1] = j - 1;
            }
        }
        return rowToCol;
    }

    /**
     * Matches volunteers to services minimizing preference cost using Volunteers.Hungarian Algorithm.
     *
     * @param prefs    volunteer preference map (volunteerId → list of preferred serviceIds in order)
     * @param services list of available serviceIds
     * @return map of volunteerId → assigned serviceId (or null if unassigned)
     */
    public static Map<String, String> optimizeHungarian(HashMap<String, ArrayList<String>> prefs, List<String> services) {
        List<String> volunteers = new ArrayList<>(prefs.keySet());
        int vCount = volunteers.size();
        int sCount = services.size();
        int n = Math.max(vCount, sCount); // Ensure square matrix

        // Pad services list with nulls to make square matrix
        List<String> paddedServices = new ArrayList<>(services);
        while (paddedServices.size() < n) paddedServices.add(null);

        // Build cost matrix
        int penalty = 1000; // Arbitrary large penalty for undesired service
        int[][] cost = new int[n][n];
        for (int i = 0; i < n; i++) {
            List<String> prefList = (i < vCount)
                    ? prefs.getOrDefault(volunteers.get(i), new ArrayList<>())
                    : Collections.emptyList();

            for (int j = 0; j < n; j++) {
                String svc = paddedServices.get(j);
                if (svc == null) {
                    cost[i][j] = penalty;
                } else {
                    int idx = prefList.indexOf(svc);
                    cost[i][j] = (idx >= 0) ? idx : penalty;
                }
            }
        }

        // Run Volunteers.Hungarian Algorithm
        Hungarian hung = new Hungarian(cost);
        int[] rowToCol = hung.execute();

        // Build result map
        Map<String, String> assignments = new HashMap<>();
        for (int i = 0; i < vCount; i++) {
            int col = rowToCol[i];
            String assignedService = (col < sCount) ? services.get(col) : null;
            if (assignedService == null || cost[i][col] == penalty) {
                assignments.put(volunteers.get(i), null); // No valid assignment
            } else {
                assignments.put(volunteers.get(i), assignedService);
            }
        }
        return assignments;
    }
}
