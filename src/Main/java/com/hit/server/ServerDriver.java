package com.hit.server;

import com.hit.service.RouteService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ServerDriver {

    public static void main(String[] args) {
        RouteService routeService = new RouteService();

        // ✅ Update path if needed (keep what exists in your project)
        loadGraphFromFile(routeService, "src/Main/resources/datasource.txt");

        Server server = new Server(34567, routeService);
        server.start();
    }

    private static void loadGraphFromFile(RouteService routeService, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // ✅ supports "1, 2, 10" and "1 2 10"
                String[] parts = line.split("[,\\s]+");
                if (parts.length < 3) continue;

                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                int w = Integer.parseInt(parts[2]);

                routeService.addEdge(u, v, w);
            }
        } catch (IOException e) {
            System.err.println("Error reading graph file: " + e.getMessage());
        }
    }
}
