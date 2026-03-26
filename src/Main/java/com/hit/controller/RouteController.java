package com.hit.controller;

import com.hit.dao.IDao;
import com.hit.dm.RouteRequest;
import com.hit.service.RouteService;

import java.util.List;

public class RouteController {

    private final RouteService routeService;
    private final IDao dao;

    public RouteController(RouteService routeService, IDao dao) {
        this.routeService = routeService;
        this.dao = dao;
    }

    public List<Integer> getShortestPath(RouteRequest request) {
        List<Integer> path = routeService.findShortestPath(request.getStart(), request.getEnd());
        dao.save(path);
        return path;
    }
}
