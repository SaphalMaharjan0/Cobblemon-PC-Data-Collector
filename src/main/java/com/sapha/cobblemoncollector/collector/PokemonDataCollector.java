package com.sapha.cobblemoncollector.collector;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.sapha.cobblemoncollector.export.CsvExporter;
import com.sapha.cobblemoncollector.export.JsonExporter;
import com.sapha.cobblemoncollector.model.PokemonExportRecord;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PokemonDataCollector {

    public record ExportResult(
            int totalCount,
            int partyCount,
            int pcCount,
            int boxCount,
            File jsonFile,
            File csvFile
    ) {}

    /**
     * Collects all Pokémon currently loaded in client memory (Party + synced PC boxes).
     */
    public static List<PokemonExportRecord> collectAll() {
        List<PokemonExportRecord> records = new ArrayList<>();
        ClientStorageManager storage = CobblemonClient.INSTANCE.getStorage();

        // 1. Collect Active Party Pokémon
        ClientParty party = storage.getParty();
        if (party != null && party.getSlots() != null) {
            int slotIdx = 1;
            for (Pokemon pokemon : party.getSlots()) {
                if (pokemon != null) {
                    records.add(fromPokemon("PARTY", 0, slotIdx, pokemon));
                }
                slotIdx++;
            }
        }

        // 2. Collect PC Pokémon from all synced PC stores
        Map<UUID, ClientPC> pcStores = storage.getPcStores();
        if (pcStores != null) {
            for (ClientPC pc : pcStores.values()) {
                if (pc == null) continue;
                List<ClientBox> boxes = pc.getBoxes();
                if (boxes == null) continue;

                for (int b = 0; b < boxes.size(); b++) {
                    ClientBox box = boxes.get(b);
                    if (box == null || box.getSlots() == null) continue;

                    int boxNumber = b + 1; // 1-indexed for display
                    for (int s = 0; s < box.getSlots().size(); s++) {
                        Pokemon pokemon = box.getSlots().get(s);
                        if (pokemon != null) {
                            records.add(fromPokemon("PC", boxNumber, s + 1, pokemon));
                        }
                    }
                }
            }
        }

        return records;
    }

    /**
     * Collects and exports to both JSON and CSV files.
     */
    public static ExportResult exportData(boolean exportJson, boolean exportCsv) throws IOException {
        List<PokemonExportRecord> records = collectAll();

        File exportDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "cobblemon_exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File jsonFile = null;
        File csvFile = null;

        if (exportJson) {
            jsonFile = new File(exportDir, "pokemon_export_" + timestamp + ".json");
            JsonExporter.export(records, jsonFile);

            // Also update a fixed "latest" file for convenience
            File latestJson = new File(exportDir, "pokemon_export_latest.json");
            JsonExporter.export(records, latestJson);
        }

        if (exportCsv) {
            csvFile = new File(exportDir, "pokemon_export_" + timestamp + ".csv");
            CsvExporter.export(records, csvFile);

            // Also update a fixed "latest" file for convenience
            File latestCsv = new File(exportDir, "pokemon_export_latest.csv");
            CsvExporter.export(records, latestCsv);
        }

        long partyCount = records.stream().filter(r -> "PARTY".equalsIgnoreCase(r.source())).count();
        long pcCount = records.stream().filter(r -> "PC".equalsIgnoreCase(r.source())).count();

        int boxCount = 0;
        Map<UUID, ClientPC> pcStores = CobblemonClient.INSTANCE.getStorage().getPcStores();
        if (pcStores != null) {
            for (ClientPC pc : pcStores.values()) {
                if (pc != null && pc.getBoxes() != null) {
                    boxCount += pc.getBoxes().size();
                }
            }
        }

        return new ExportResult(records.size(), (int) partyCount, (int) pcCount, boxCount, jsonFile, csvFile);
    }

    private static PokemonExportRecord fromPokemon(String source, int boxNumber, int slotNumber, Pokemon pokemon) {
        String speciesName = pokemon.getSpecies() != null ? pokemon.getSpecies().getName() : "Unknown";
        String formName = pokemon.getForm() != null ? pokemon.getForm().getName() : "Normal";
        String displayName = pokemon.getDisplayName(false) != null ? pokemon.getDisplayName(false).getString() : speciesName;
        String gender = pokemon.getGender() != null ? pokemon.getGender().name() : "GENDERLESS";

        int level = pokemon.getLevel();
        boolean shiny = pokemon.getShiny();

        String ability = pokemon.getAbility() != null ? pokemon.getAbility().getName() : "Unknown";
        String nature = pokemon.getNature() != null ? pokemon.getNature().getDisplayName() : "Unknown";

        // IVs
        int hpIV = getStatIV(pokemon, Stats.HP);
        int atkIV = getStatIV(pokemon, Stats.ATTACK);
        int defIV = getStatIV(pokemon, Stats.DEFENCE);
        int spAtkIV = getStatIV(pokemon, Stats.SPECIAL_ATTACK);
        int spDefIV = getStatIV(pokemon, Stats.SPECIAL_DEFENCE);
        int speedIV = getStatIV(pokemon, Stats.SPEED);

        int totalIVSum = hpIV + atkIV + defIV + spAtkIV + spDefIV + speedIV;
        double totalIVPct = (totalIVSum / 186.0) * 100.0;

        int perfectCount = 0;
        int[] ivArray = new int[] { hpIV, atkIV, defIV, spAtkIV, spDefIV, speedIV };
        for (int iv : ivArray) {
            if (iv == 31) perfectCount++;
        }

        // Egg Groups
        List<String> eggGroupNames = new ArrayList<>();
        Set<EggGroup> eggGroups = null;
        if (pokemon.getForm() != null) {
            eggGroups = pokemon.getForm().getEggGroups();
        }
        if ((eggGroups == null || eggGroups.isEmpty()) && pokemon.getSpecies() != null) {
            eggGroups = pokemon.getSpecies().getEggGroups();
        }
        if (eggGroups != null) {
            for (EggGroup eg : eggGroups) {
                if (eg != null) {
                    eggGroupNames.add(eg.name());
                }
            }
        }
        if (eggGroupNames.isEmpty()) {
            eggGroupNames.add("Undiscovered");
        }

        // Moves
        List<String> moves = new ArrayList<>();
        if (pokemon.getMoveSet() != null) {
            for (Move move : pokemon.getMoveSet()) {
                if (move != null && move.getDisplayName() != null) {
                    moves.add(move.getDisplayName().getString());
                }
            }
        }

        // Held item
        String heldItem = "None";
        if (pokemon.heldItem() != null && !pokemon.heldItem().isEmpty()) {
            heldItem = pokemon.heldItem().getItem().toString();
        }

        String uuid = pokemon.getUuid() != null ? pokemon.getUuid().toString() : "";

        return new PokemonExportRecord(
                source,
                boxNumber,
                slotNumber,
                speciesName,
                formName,
                displayName,
                gender,
                level,
                shiny,
                ability,
                nature,
                hpIV,
                atkIV,
                defIV,
                spAtkIV,
                spDefIV,
                speedIV,
                totalIVPct,
                perfectCount,
                eggGroupNames,
                moves,
                heldItem,
                uuid
        );
    }

    private static int getStatIV(Pokemon pokemon, Stat stat) {
        if (pokemon.getIvs() == null || stat == null) {
            return 0;
        }
        return pokemon.getIvs().getOrDefault(stat);
    }
}
