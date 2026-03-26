package com.hit.service;

import com.hit.algorithms.shortestpath.BellmanFordShortestPath;
import com.hit.algorithms.shortestpath.DijkstraShortestPath;
import com.hit.algorithms.shortestpath.Graph;
import com.hit.algorithms.shortestpath.IAlgoShortestPath;

import java.util.*;

public class RouteService {

    // נשמור adjacency בשביל חישוב cost מהנתיב שחוזר
    private final Map<Integer, List<int[]>> adj = new HashMap<Integer, List<int[]>>();
    private final List<int[]> allEdges = new ArrayList<int[]>();
    private int maxNode = 0;

    // graph של מודול האלגוריתמים (מה־JAR)
    private Graph algoGraph = null;

    // Strategy: האלגוריתם שבו משתמשים (חובה לפי הדרישות)
    private final IAlgoShortestPath shortestPathAlgo;

    // cache לזוגות (u,v) כדי לא לחשב דייקסטרה שוב ושוב
    private final Map<Long, List<Integer>> pathCache = new HashMap<Long, List<Integer>>();
    private final Map<Long, Integer> costCache = new HashMap<Long, Integer>();

    // ברירת מחדל: Dijkstra
    public RouteService() {
        this(new DijkstraShortestPath());
    }

    // מאפשר החלפת strategy (למשל Bellman-Ford)
    public RouteService(IAlgoShortestPath algo) {
        this.shortestPathAlgo = algo;
    }

    public void addEdge(int u, int v, int w) {
        maxNode = Math.max(maxNode, Math.max(u, v));

        adj.computeIfAbsent(u, k -> new ArrayList<int[]>()).add(new int[]{v, w});
        adj.computeIfAbsent(v, k -> new ArrayList<int[]>());

        allEdges.add(new int[]{u, v, w});

        // invalidate graph/cache
        algoGraph = null;
        pathCache.clear();
        costCache.clear();
    }

    // בונים Graph של מודול האלגוריתמים פעם אחת אחרי טעינה
    public void buildGraphIfNeeded() {
        if (algoGraph != null) return;

        algoGraph = new Graph(maxNode + 1);
        for (int[] e : allEdges) {
            algoGraph.addEdge(e[0], e[1], e[2]);
        }
    }

    // shortest path באמצעות האלגוריתם שלכם
    public List<Integer> findShortestPath(int start, int end) {
        buildGraphIfNeeded();
        if (start == end) {
            List<Integer> single = new ArrayList<Integer>();
            single.add(start);
            return single;
        }
        List<Integer> path = shortestPathAlgo.findShortestPath(algoGraph, start, end);
        return (path == null) ? Collections.<Integer>emptyList() : path;
    }

    // ✅ הדרישה שלך: סט של stops, הסדר לא חשוב → לבחור סדר אופטימלי (DP)
    public List<Integer> findBestRouteAnyOrder(int start, int end, List<Integer> stops) {
        buildGraphIfNeeded();

        // normalize stops (remove null/duplicates/start/end)
        List<Integer> uniqStops = new ArrayList<Integer>();
        HashSet<Integer> seen = new HashSet<Integer>();

        if (stops != null) {
            for (Integer s : stops) {
                if (s == null) continue;
                if (s == start || s == end) continue;
                if (seen.add(s)) uniqStops.add(s);
            }
        }

        if (uniqStops.isEmpty()) {
            return getShortestPathCached(start, end);
        }

        final int k = uniqStops.size();
        final int ALL = (1 << k) - 1;
        final int INF = Integer.MAX_VALUE / 4;

        int[][] dp = new int[1 << k][k];
        int[][] parent = new int[1 << k][k];

        for (int mask = 0; mask <= ALL; mask++) {
            for (int i = 0; i < k; i++) {
                dp[mask][i] = INF;
                parent[mask][i] = -1;
            }
        }

        // init: start -> stop[i]
        for (int i = 0; i < k; i++) {
            int si = uniqStops.get(i);
            int c = getShortestCostCached(start, si);
            if (c < INF) dp[1 << i][i] = c;
        }

        // transitions
        for (int mask = 1; mask <= ALL; mask++) {
            for (int last = 0; last < k; last++) {
                if ((mask & (1 << last)) == 0) continue;
                if (dp[mask][last] >= INF) continue;

                for (int nxt = 0; nxt < k; nxt++) {
                    if ((mask & (1 << nxt)) != 0) continue;

                    int a = uniqStops.get(last);
                    int b = uniqStops.get(nxt);
                    int c = getShortestCostCached(a, b);
                    if (c >= INF) continue;

                    int nmask = mask | (1 << nxt);
                    int nd = dp[mask][last] + c;

                    if (nd < dp[nmask][nxt]) {
                        dp[nmask][nxt] = nd;
                        parent[nmask][nxt] = last;
                    }
                }
            }
        }

        // finish: lastStop -> end
        int best = INF;
        int bestLast = -1;

        for (int last = 0; last < k; last++) {
            if (dp[ALL][last] >= INF) continue;
            int a = uniqStops.get(last);
            int c = getShortestCostCached(a, end);
            if (c >= INF) continue;

            int total = dp[ALL][last] + c;
            if (total < best) {
                best = total;
                bestLast = last;
            }
        }

        if (bestLast == -1) return Collections.emptyList();

        // reconstruct order of stops
        LinkedList<Integer> order = new LinkedList<Integer>();
        int mask = ALL;
        int cur = bestLast;

        while (cur != -1) {
            order.addFirst(uniqStops.get(cur));
            int p = parent[mask][cur];
            mask ^= (1 << cur);
            cur = p;
        }

        // build full route by concatenating shortest paths between consecutive points
        List<Integer> route = new ArrayList<Integer>();
        int prev = start;

        for (Integer s : order) {
            List<Integer> seg = getShortestPathCached(prev, s);
            if (seg.isEmpty()) return Collections.emptyList();
            merge(route, seg);
            prev = s;
        }

        List<Integer> lastSeg = getShortestPathCached(prev, end);
        if (lastSeg.isEmpty()) return Collections.emptyList();
        merge(route, lastSeg);

        return route;
    }

    public int calculateRouteCost(List<Integer> route) {
        if (route == null || route.size() < 2) return 0;

        int cost = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            int u = route.get(i);
            int v = route.get(i + 1);
            int w = getEdgeWeight(u, v);
            if (w == Integer.MAX_VALUE) return Integer.MAX_VALUE;
            cost += w;
        }
        return cost;
    }

    // -------- helpers --------

    private void merge(List<Integer> full, List<Integer> seg) {
        if (full.isEmpty()) full.addAll(seg);
        else full.addAll(seg.subList(1, seg.size()));
    }

    private int getEdgeWeight(int u, int v) {
        List<int[]> edges = adj.get(u);
        if (edges == null) return Integer.MAX_VALUE;
        for (int[] e : edges) {
            if (e[0] == v) return e[1];
        }
        return Integer.MAX_VALUE;
    }

    private long key(int u, int v) {
        return (((long) u) << 32) ^ (v & 0xffffffffL);
    }

    private List<Integer> getShortestPathCached(int u, int v) {
        long k = key(u, v);
        List<Integer> cached = pathCache.get(k);
        if (cached != null) return cached;

        List<Integer> path = findShortestPath(u, v);
        if (path == null) path = Collections.emptyList();

        pathCache.put(k, path);
        costCache.put(k, path.isEmpty() ? Integer.MAX_VALUE / 4 : calculateRouteCost(path));
        return path;
    }

    private int getShortestCostCached(int u, int v) {
        long k = key(u, v);
        Integer c = costCache.get(k);
        if (c != null) return c;
        getShortestPathCached(u, v);
        return costCache.get(k);
    }

    // אם תרצי להחליף לאלגוריתם אחר (Strategy):
    // new RouteService(new BellmanFordShortestPath());
}
