package com.hit.dm;

import java.util.List;

public class RouteResponse {
    private List<Integer> route;
    private int cost; // sum of weights

    public RouteResponse() {}

    public RouteResponse(List<Integer> route, int cost) {
        this.route = route;
        this.cost = cost;
    }

    public List<Integer> getRoute() {
        return route;
    }

    public int getCost() {
        return cost;
    }

    public void setRoute(List<Integer> route) {
        this.route = route;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
