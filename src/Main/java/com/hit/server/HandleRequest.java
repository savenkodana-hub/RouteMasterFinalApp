package com.hit.server;

import com.google.gson.Gson;
import com.hit.dm.RouteRequest;
import com.hit.dm.RouteResponse;
import com.hit.service.RouteService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class HandleRequest implements Runnable {

    private final Socket clientSocket;
    private final RouteService routeService;
    private final Gson gson = new Gson();

    public HandleRequest(Socket clientSocket, RouteService routeService) {
        this.clientSocket = clientSocket;
        this.routeService = routeService;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String jsonReq = in.readLine();
            if (jsonReq == null || jsonReq.isEmpty()) {
                RouteResponse resp = new RouteResponse(Collections.<Integer>emptyList(), 0);
                out.println(gson.toJson(resp));
                return;
            }

            RouteRequest req = gson.fromJson(jsonReq, RouteRequest.class);

            // ✅ ANY ORDER optimal
            List<Integer> route = routeService.findBestRouteAnyOrder(
                    req.getStart(), req.getEnd(), req.getStops()
            );

            int cost = route.isEmpty() ? 0 : routeService.calculateRouteCost(route);
            RouteResponse resp = new RouteResponse(route, cost);

            out.println(gson.toJson(resp));

        } catch (IOException e) {
            System.err.println("Request handling error: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }
}
