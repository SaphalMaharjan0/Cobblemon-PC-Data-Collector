package com.sapha.cobblemoncollector.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sapha.cobblemoncollector.model.PokedexExportRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PokedexJsonExporter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static File export(List<PokedexExportRecord> records, File targetFile) throws IOException {
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        long uniqueSpeciesCount = records.stream().map(PokedexExportRecord::species).distinct().count();

        JsonObject root = new JsonObject();
        root.addProperty("exportedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        root.addProperty("statusFilter", "CAUGHT");
        root.addProperty("totalCaughtEntries", records.size());
        root.addProperty("uniqueSpeciesCount", uniqueSpeciesCount);
        root.add("pokemon", GSON.toJsonTree(records));

        try (FileWriter writer = new FileWriter(targetFile, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }

        return targetFile;
    }
}
