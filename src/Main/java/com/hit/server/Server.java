package com.hit.server;

import com.hit.service.RouteService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final int port;
    private final RouteService routeService;

    public Server(int port, RouteService routeService) {
        this.port = port;
        this.routeService = routeService;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new HandleRequest(client, routeService)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
