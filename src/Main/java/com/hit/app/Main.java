package com.hit.app;

import com.hit.service.RouteService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        RouteService routeService = new RouteService();

        // load graph
        loadGraphFromFile(routeService, "src/Main/resources/datasource.txt");
        routeService.buildGraphIfNeeded();

        System.out.println("✅ Graph loaded successfully!");

        // input
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter start node: ");
        int start = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter end node: ");
        int end = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter stops (comma or space separated, optional): ");
        String stopsInput = scanner.nextLine().trim();

        List<Integer> stops = parseStops(stopsInput);

        // ✅ ANY ORDER: find best route that visits all stops in any order
        List<Integer> route = routeService.findBestRouteAnyOrder(start, end, stops);

        if (route == null || route.isEmpty()) {
            System.out.println("❌ No route found that passes through all required stops.");
        } else {
            int cost = routeService.calculateRouteCost(route);
            System.out.println("✅ Best route (any order): " + route);
            System.out.println("✅ Total cost: " + cost);
        }
    }

    // --------------------------------------------
    // Load graph from file
    // Supports both:
    //   1 2 10
    //   1, 2, 10
    // --------------------------------------------
    private static void loadGraphFromFile(RouteService routeService, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("[,\\s]+");
                if (parts.length < 3) continue;

                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                int w = Integer.parseInt(parts[2]);

                routeService.addEdge(u, v, w);
            }

        } catch (IOException e) {
            System.err.println("❌ Error reading graph file: " + e.getMessage());
        }
    }

    private static List<Integer> parseStops(String input) {
        List<Integer> stops = new ArrayList<Integer>();

        if (input == null) return stops;
        input = input.trim();
        if (input.isEmpty()) return stops;

        String[] tokens = input.split("[,\\s]+");
        for (String t : tokens) {
            if (!t.isEmpty()) {
                stops.add(Integer.parseInt(t));
            }
        }
        return stops;
    }
}
