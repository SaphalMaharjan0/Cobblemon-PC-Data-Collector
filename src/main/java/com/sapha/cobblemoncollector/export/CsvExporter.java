package com.sapha.cobblemoncollector.export;

import com.sapha.cobblemoncollector.model.PokemonExportRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvExporter {

    private static final String CSV_HEADER =
            "Source,Box,Slot,Species,Form,DisplayName,Gender,Level,Shiny,Ability,Nature,HP_IV,Atk_IV,Def_IV,SpAtk_IV,SpDef_IV,Speed_IV,Total_IV_Pct,Perfect_IVs,Egg_Groups,Moves,Held_Item,UUID";

    public static File export(List<PokemonExportRecord> records, File targetFile) throws IOException {
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (PokemonExportRecord record : records) {
                StringBuilder line = new StringBuilder();
                appendCell(line, record.source());
                appendCell(line, record.boxNumber() > 0 ? String.valueOf(record.boxNumber()) : "N/A");
                appendCell(line, String.valueOf(record.slotNumber()));
                appendCell(line, record.species());
                appendCell(line, record.form());
                appendCell(line, record.displayName());
                appendCell(line, record.gender());
                appendCell(line, String.valueOf(record.level()));
                appendCell(line, String.valueOf(record.isShiny()));
                appendCell(line, record.ability());
                appendCell(line, record.nature());
                appendCell(line, String.valueOf(record.hpIV()));
                appendCell(line, String.valueOf(record.atkIV()));
                appendCell(line, String.valueOf(record.defIV()));
                appendCell(line, String.valueOf(record.spAtkIV()));
                appendCell(line, String.valueOf(record.spDefIV()));
                appendCell(line, String.valueOf(record.speedIV()));
                appendCell(line, String.format("%.1f%%", record.totalIVPercentage()));
                appendCell(line, String.valueOf(record.perfectIVCount()));
                appendCell(line, String.join("; ", record.eggGroups()));
                appendCell(line, String.join("; ", record.moves()));
                appendCell(line, record.heldItem());
                line.append(escapeCsv(record.uuid())); // Last column, no trailing comma

                writer.write(line.toString());
                writer.newLine();
            }
        }

        return targetFile;
    }

    private static void appendCell(StringBuilder sb, String value) {
        sb.append(escapeCsv(value)).append(",");
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
