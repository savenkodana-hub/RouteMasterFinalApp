package com.hit.dao;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DaoFileImpl implements IDao {

    private static final String OUTPUT_FILE = "datasource_output.txt";
    private final Gson gson = new Gson();

    @Override
    public void save(List<Integer> path) {
        try (FileWriter writer = new FileWriter(OUTPUT_FILE, true)) {
            writer.write(gson.toJson(path));
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Failed to save path: " + e.getMessage());
        }
    }
}
