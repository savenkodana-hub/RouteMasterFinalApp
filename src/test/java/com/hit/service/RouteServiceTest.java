package com.hit.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RouteServiceTest {

    @Test
    public void testShortestPath() {
        RouteService s = new RouteService();
        s.addEdge(1, 2, 5);
        s.addEdge(2, 3, 5);
        s.addEdge(1, 3, 15);

        List<Integer> path = s.findShortestPath(1, 3);
        assertEquals(List.of(1, 2, 3), path);
    }

    @Test
    public void testNoPath() {
        RouteService s = new RouteService();
        s.addEdge(1, 2, 5);
        s.addEdge(3, 4, 5);

        List<Integer> path = s.findShortestPath(1, 4);
        assertTrue(path.isEmpty());
    }
}
