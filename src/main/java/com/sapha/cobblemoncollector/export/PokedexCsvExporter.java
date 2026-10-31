package com.sapha.cobblemoncollector.export;

import com.sapha.cobblemoncollector.model.PokedexExportRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PokedexCsvExporter {

    private static final String CSV_HEADER =
            "DexNumber,Species,Identifier,Form,DisplayName,Status,CaughtShiny,CaughtGenders,HighestLevel,PrimaryType,SecondaryType,BaseHP,BaseAtk,BaseDef,BaseSpAtk,BaseSpDef,BaseSpeed,TotalBaseStats,Abilities,EggGroups,CatchRate,MaleRatio,BaseExperience,BaseFriendship,Height,Weight,Labels,Aspects";

    public static File export(List<PokedexExportRecord> records, File targetFile) throws IOException {
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (PokedexExportRecord record : records) {
                StringBuilder line = new StringBuilder();
                appendCell(line, String.valueOf(record.dexNumber()));
                appendCell(line, record.species());
                appendCell(line, record.speciesIdentifier());
                appendCell(line, record.form());
                appendCell(line, record.displayName());
                appendCell(line, record.status());
                appendCell(line, String.valueOf(record.caughtShiny()));
                appendCell(line, String.join("; ", record.caughtGenders()));
                appendCell(line, String.valueOf(record.highestLevel()));
                appendCell(line, record.primaryType());
                appendCell(line, record.secondaryType());
                appendCell(line, String.valueOf(record.baseHp()));
                appendCell(line, String.valueOf(record.baseAttack()));
                appendCell(line, String.valueOf(record.baseDefense()));
                appendCell(line, String.valueOf(record.baseSpAttack()));
                appendCell(line, String.valueOf(record.baseSpDefense()));
                appendCell(line, String.valueOf(record.baseSpeed()));
                appendCell(line, String.valueOf(record.totalBaseStats()));
                appendCell(line, String.join("; ", record.abilities()));
                appendCell(line, String.join("; ", record.eggGroups()));
                appendCell(line, String.valueOf(record.catchRate()));
                appendCell(line, record.maleRatio() >= 0 ? String.format("%.1f%%", record.maleRatio() * 100) : "Genderless");
                appendCell(line, String.valueOf(record.baseExperience()));
                appendCell(line, String.valueOf(record.baseFriendship()));
                appendCell(line, String.format("%.1fm", record.height()));
                appendCell(line, String.format("%.1fkg", record.weight()));
                appendCell(line, String.join("; ", record.labels()));
                line.append(escapeCsv(String.join("; ", record.aspects())));

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
