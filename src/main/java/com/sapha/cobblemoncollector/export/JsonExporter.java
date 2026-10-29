package com.sapha.cobblemoncollector.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sapha.cobblemoncollector.model.PokemonExportRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JsonExporter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static File export(List<PokemonExportRecord> records, File targetFile) throws IOException {
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        long partyCount = records.stream().filter(r -> "PARTY".equalsIgnoreCase(r.source())).count();
        long pcCount = records.stream().filter(r -> "PC".equalsIgnoreCase(r.source())).count();

        JsonObject root = new JsonObject();
        root.addProperty("exportedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        root.addProperty("totalCount", records.size());
        root.addProperty("partyCount", partyCount);
        root.addProperty("pcCount", pcCount);
        root.add("pokemon", GSON.toJsonTree(records));

        try (FileWriter writer = new FileWriter(targetFile, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }

        return targetFile;
    }
}
