package com.hit.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CityCatalog {

    public static class CityItem {
        private final int id;
        private final String name;

        public CityItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name;
        }
    }

    private final ObservableList<CityItem> cities = FXCollections.observableArrayList();
    private final Map<String, Integer> nameToId = new HashMap<String, Integer>();

    public CityCatalog(String filePath) throws IOException {
        load(filePath);
    }

    private void load(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // supports: "1,Tel Aviv" OR "1, Tel Aviv"
                String[] parts = line.split("\\s*,\\s*", 2);
                if (parts.length < 2) continue;

                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();

                CityItem item = new CityItem(id, name);
                cities.add(item);
                nameToId.put(name, id);
            }
        } finally {
            br.close();
        }
    }

    public ObservableList<CityItem> getCities() {
        return cities;
    }

    public Integer getIdByName(String name) {
        return nameToId.get(name);
    }
}
