package com.hit.client;

import com.google.gson.Gson;
import com.hit.dm.RouteRequest;
import com.hit.dm.RouteResponse;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class MyClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 34567;

    private final Gson gson = new Gson();

    public RouteResponse requestRoute(int start, int end, List<Integer> stops) throws IOException {
        RouteRequest req = new RouteRequest(start, end, stops);
        String jsonReq = gson.toJson(req);

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(jsonReq);

            String jsonResp = in.readLine();
            if (jsonResp == null || jsonResp.isEmpty()) {
                return new RouteResponse(Collections.<Integer>emptyList(), 0);
            }

            return gson.fromJson(jsonResp, RouteResponse.class);
        }
    }
}
