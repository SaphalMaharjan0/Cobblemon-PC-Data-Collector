package com.sapha.cobblemoncollector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sapha.cobblemoncollector.export.PokedexCsvExporter;
import com.sapha.cobblemoncollector.export.PokedexJsonExporter;
import com.sapha.cobblemoncollector.model.PokedexExportRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PokedexExportTest {

    @Test
    public void testPokedexExportRecordProperties() {
        PokedexExportRecord record = new PokedexExportRecord(
                25,
                "Pikachu",
                "cobblemon:pikachu",
                "Normal",
                "Pikachu",
                "CAUGHT",
                true,
                List.of("MALE", "FEMALE"),
                50,
                "ELECTRIC",
                "None",
                35,
                55,
                40,
                50,
                50,
                90,
                320,
                List.of("Static", "Lightning Rod (Hidden)"),
                List.of("Field", "Fairy"),
                190,
                0.5f,
                112,
                70,
                0.4f,
                6.0f,
                List.of("gen1"),
                List.of()
        );

        assertEquals(25, record.dexNumber());
        assertEquals("Pikachu", record.species());
        assertEquals("cobblemon:pikachu", record.speciesIdentifier());
        assertEquals("Normal", record.form());
        assertEquals("Pikachu", record.displayName());
        assertEquals("CAUGHT", record.status());
        assertTrue(record.caughtShiny());
        assertEquals(List.of("MALE", "FEMALE"), record.caughtGenders());
        assertEquals(50, record.highestLevel());
        assertEquals("ELECTRIC", record.primaryType());
        assertEquals("None", record.secondaryType());
        assertEquals(35, record.baseHp());
        assertEquals(55, record.baseAttack());
        assertEquals(40, record.baseDefense());
        assertEquals(50, record.baseSpAttack());
        assertEquals(50, record.baseSpDefense());
        assertEquals(90, record.baseSpeed());
        assertEquals(320, record.totalBaseStats());
        assertEquals(2, record.abilities().size());
        assertEquals(190, record.catchRate());
        assertEquals(0.5f, record.maleRatio());
        assertEquals(0.4f, record.height());
        assertEquals(6.0f, record.weight());
    }

    @Test
    public void testPokedexCsvExport(@TempDir Path tempDir) throws IOException {
        PokedexExportRecord pikachu = new PokedexExportRecord(
                25,
                "Pikachu",
                "cobblemon:pikachu",
                "Normal",
                "Pikachu",
                "CAUGHT",
                true,
                List.of("MALE", "FEMALE"),
                50,
                "ELECTRIC",
                "None",
                35,
                55,
                40,
                50,
                50,
                90,
                320,
                List.of("Static", "Lightning Rod (Hidden)"),
                List.of("Field", "Fairy"),
                190,
                0.5f,
                112,
                70,
                0.4f,
                6.0f,
                List.of("gen1"),
                List.of("region_kanto")
        );

        PokedexExportRecord charizard = new PokedexExportRecord(
                6,
                "Charizard",
                "cobblemon:charizard",
                "Normal",
                "Charizard",
                "CAUGHT",
                false,
                List.of("MALE"),
                100,
                "FIRE",
                "FLYING",
                78,
                84,
                78,
                109,
                85,
                100,
                534,
                List.of("Blaze", "Solar Power (Hidden)"),
                List.of("Monster", "Dragon"),
                45,
                0.875f,
                240,
                70,
                1.7f,
                90.5f,
                List.of("gen1", "starter"),
                List.of()
        );

        File targetCsv = tempDir.resolve("test_pokedex.csv").toFile();
        PokedexCsvExporter.export(List.of(pikachu, charizard), targetCsv);

        assertTrue(targetCsv.exists());
        List<String> lines = Files.readAllLines(targetCsv.toPath());
        assertEquals(3, lines.size()); // Header + 2 rows

        // Check header
        assertTrue(lines.get(0).startsWith("DexNumber,Species,Identifier,Form,DisplayName,Status"));

        // Check rows contain CAUGHT
        assertTrue(lines.get(1).contains("CAUGHT"));
        assertTrue(lines.get(1).contains("Pikachu"));
        assertTrue(lines.get(1).contains("ELECTRIC"));

        assertTrue(lines.get(2).contains("CAUGHT"));
        assertTrue(lines.get(2).contains("Charizard"));
        assertTrue(lines.get(2).contains("FIRE"));
        assertTrue(lines.get(2).contains("FLYING"));
    }

    @Test
    public void testPokedexJsonExport(@TempDir Path tempDir) throws IOException {
        PokedexExportRecord vulpix = new PokedexExportRecord(
                37,
                "Vulpix",
                "cobblemon:vulpix",
                "Alolan",
                "Alolan Vulpix",
                "CAUGHT",
                false,
                List.of("FEMALE"),
                22,
                "ICE",
                "None",
                38,
                41,
                40,
                50,
                65,
                65,
                299,
                List.of("Snow Cloak", "Snow Warning (Hidden)"),
                List.of("Field"),
                190,
                0.25f,
                60,
                70,
                0.6f,
                9.9f,
                List.of("gen7", "alolan"),
                List.of()
        );

        File targetJson = tempDir.resolve("test_pokedex.json").toFile();
        PokedexJsonExporter.export(List.of(vulpix), targetJson);

        assertTrue(targetJson.exists());
        String content = Files.readString(targetJson.toPath());

        JsonObject root = JsonParser.parseString(content).getAsJsonObject();
        assertEquals("CAUGHT", root.get("statusFilter").getAsString());
        assertEquals(1, root.get("totalCaughtEntries").getAsInt());
        assertEquals(1, root.get("uniqueSpeciesCount").getAsInt());
        assertTrue(root.has("exportedAt"));
        assertTrue(root.has("pokemon"));

        var array = root.getAsJsonArray("pokemon");
        assertEquals(1, array.size());
        JsonObject first = array.get(0).getAsJsonObject();
        assertEquals(37, first.get("dexNumber").getAsInt());
        assertEquals("Vulpix", first.get("species").getAsString());
        assertEquals("Alolan", first.get("form").getAsString());
        assertEquals("CAUGHT", first.get("status").getAsString());
        assertEquals("ICE", first.get("primaryType").getAsString());
    }

    @Test
    public void testFormDexRecordUninitializedLateinitSafety() {
        // Simulates the network sync condition where Cobblemon's bytebuf decoder creates
        // a FormDexRecord without calling initialize, leaving formName uninitialized.
        com.cobblemon.mod.common.api.pokedex.FormDexRecord uninitializedRecord = new com.cobblemon.mod.common.api.pokedex.FormDexRecord();

        // Calling uninitializedRecord.getFormName() directly would throw kotlin.UninitializedPropertyAccessException
        assertThrows(Throwable.class, uninitializedRecord::getFormName);

        // Verifying that setFormName makes it safe
        uninitializedRecord.setFormName("alolan");
        assertEquals("alolan", uninitializedRecord.getFormName());
    }
}
