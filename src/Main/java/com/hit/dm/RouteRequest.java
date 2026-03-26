package com.hit.dm;

import java.util.List;

public class RouteRequest {
    private int start;
    private int end;
    private List<Integer> stops;

    public RouteRequest(int start, int end, List<Integer> stops) {
        this.start = start;
        this.end = end;
        this.stops = stops;
    }

    // Gson needs a no-args constructor sometimes (safe to include)
    public RouteRequest() {}

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public List<Integer> getStops() {
        return stops;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setStops(List<Integer> stops) {
        this.stops = stops;
    }

    @Override
    public String toString() {
        return "RouteRequest{start=" + start + ", end=" + end + ", stops=" + stops + '}';
    }
}
