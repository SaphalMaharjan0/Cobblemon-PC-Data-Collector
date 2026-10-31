package com.sapha.cobblemoncollector.collector;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.abilities.AbilityPool;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.pokedex.AbstractPokedexManager;
import com.cobblemon.mod.common.api.pokedex.FormDexRecord;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokedex.SpeciesDexRecord;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Species;
import com.sapha.cobblemoncollector.export.PokedexCsvExporter;
import com.sapha.cobblemoncollector.export.PokedexJsonExporter;
import com.sapha.cobblemoncollector.model.PokedexExportRecord;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PokedexDataCollector {

    private static final Field FORM_RECORDS_FIELD;

    static {
        Field field = null;
        try {
            field = SpeciesDexRecord.class.getDeclaredField("formRecords");
            field.setAccessible(true);
        } catch (Throwable ignored) {}
        FORM_RECORDS_FIELD = field;
    }

    public record PokedexExportResult(
            int totalCount,
            int uniqueSpeciesCount,
            File jsonFile,
            File csvFile
    ) {}

    private record CaughtFormEntry(
            String formKey,
            FormDexRecord record
    ) {}

    /**
     * Retrieves active Pokédex manager (client Pokédex or singleplayer server Pokédex as fallback).
     */
    public static AbstractPokedexManager getActivePokedexManager() {
        try {
            AbstractPokedexManager clientDex = CobblemonClient.INSTANCE.getClientPokedexData();
            if (clientDex != null && clientDex.getSpeciesRecords() != null && !clientDex.getSpeciesRecords().isEmpty()) {
                return clientDex;
            }
        } catch (Throwable ignored) {}

        // Fallback for singleplayer / integrated test server
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null && mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
                UUID playerId = mc.player.getUUID();
                AbstractPokedexManager serverDex = Cobblemon.INSTANCE.getPlayerDataManager().getPokedexData(playerId);
                if (serverDex != null) {
                    return serverDex;
                }
            }
        } catch (Throwable ignored) {}

        return CobblemonClient.INSTANCE.getClientPokedexData();
    }

    /**
     * Collects all Pokémon currently registered as CAUGHT (OWNED) in player's Pokédex.
     */
    public static List<PokedexExportRecord> collectAllCaught() {
        List<PokedexExportRecord> records = new ArrayList<>();
        AbstractPokedexManager pokedex = getActivePokedexManager();
        if (pokedex == null) {
            return records;
        }

        Map<ResourceLocation, SpeciesDexRecord> speciesRecords = pokedex.getSpeciesRecords();
        if (speciesRecords == null || speciesRecords.isEmpty()) {
            return records;
        }

        for (Map.Entry<ResourceLocation, SpeciesDexRecord> entry : speciesRecords.entrySet()) {
            try {
                ResourceLocation speciesId = entry.getKey();
                SpeciesDexRecord speciesRecord = entry.getValue();
                if (speciesId == null || speciesRecord == null) continue;

                // Ensure lateinit fields on speciesRecord are initialized if unpopulated by server sync
                try {
                    speciesRecord.setId(speciesId);
                } catch (Throwable ignored) {}
                try {
                    speciesRecord.setPokedexManager(pokedex);
                } catch (Throwable ignored) {}

                // Check if species is marked as OWNED (CAUGHT)
                boolean isSpeciesCaught = false;
                try {
                    isSpeciesCaught = (speciesRecord.getKnowledge() == PokedexEntryProgress.OWNED)
                            || speciesRecord.hasAtLeast(PokedexEntryProgress.OWNED);
                } catch (Throwable ignored) {}

                Species species = null;
                try {
                    species = PokemonSpecies.getByIdentifier(speciesId);
                } catch (Throwable ignored) {}

                // Extract all form records and check which specific forms are CAUGHT
                Map<String, FormDexRecord> formRecords = extractFormRecords(speciesRecord, speciesId);
                List<CaughtFormEntry> caughtForms = new ArrayList<>();

                if (formRecords != null && !formRecords.isEmpty()) {
                    for (Map.Entry<String, FormDexRecord> formEntry : formRecords.entrySet()) {
                        String formKey = formEntry.getKey();
                        FormDexRecord fr = formEntry.getValue();
                        if (fr == null) continue;

                        // Safely initialize lateinit properties on FormDexRecord to prevent Kotlin uninitialized exceptions
                        try {
                            fr.setFormName(formKey != null ? formKey : "normal");
                        } catch (Throwable ignored) {}
                        try {
                            fr.setSpeciesDexRecord(speciesRecord);
                        } catch (Throwable ignored) {}

                        boolean isFormCaught = false;
                        try {
                            isFormCaught = (fr.getKnowledge() == PokedexEntryProgress.OWNED);
                        } catch (Throwable ignored) {}

                        if (isFormCaught) {
                            caughtForms.add(new CaughtFormEntry(formKey, fr));
                        }
                    }
                }

                // If no individual form has OWNED status, but species record is OWNED
                if (caughtForms.isEmpty()) {
                    if (!isSpeciesCaught) {
                        // Not caught at all (e.g. only SEEN or UNREGISTERED) -> Skip!
                        continue;
                    }

                    // Species caught: create record for standard form
                    FormData standardForm = species != null ? species.getStandardForm() : null;
                    String standardFormName = standardForm != null ? standardForm.getName() : "Normal";
                    FormDexRecord standardFr = null;
                    try {
                        standardFr = speciesRecord.getFormRecord(standardFormName);
                        if (standardFr != null) {
                            try { standardFr.setFormName(standardFormName); } catch (Throwable ignored) {}
                            try { standardFr.setSpeciesDexRecord(speciesRecord); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}

                    records.add(buildRecord(speciesId, species, standardForm, standardFr, speciesRecord, standardFormName));
                } else {
                    // Export each form that was registered as CAUGHT
                    for (CaughtFormEntry cfe : caughtForms) {
                        String formKey = cfe.formKey();
                        FormDexRecord fr = cfe.record();

                        FormData formData = null;
                        if (species != null) {
                            if (formKey != null) {
                                formData = species.getFormByName(formKey);
                                if (formData == null && species.getForms() != null) {
                                    for (FormData fd : species.getForms()) {
                                        if (fd != null && fd.getName() != null && fd.getName().equalsIgnoreCase(formKey)) {
                                            formData = fd;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (formData == null && ("normal".equalsIgnoreCase(formKey) || formKey == null || formKey.isBlank())) {
                                formData = species.getStandardForm();
                            }
                        }

                        String displayFormName = resolveFormDisplayName(formKey, formData, fr);
                        records.add(buildRecord(speciesId, species, formData, fr, speciesRecord, displayFormName));
                    }
                }
            } catch (Throwable t) {
                // Protect loop: an issue with one species record should not abort the entire export
                t.printStackTrace();
            }
        }

        // Sort by National Pokédex number ascending, then by species name, then form
        records.sort(Comparator
                .comparingInt(PokedexExportRecord::dexNumber)
                .thenComparing(PokedexExportRecord::species)
                .thenComparing(PokedexExportRecord::form));

        return records;
    }

    /**
     * Collects and exports caught Pokédex data to JSON and/or CSV files.
     */
    public static PokedexExportResult exportData(boolean exportJson, boolean exportCsv) throws IOException {
        List<PokedexExportRecord> records = collectAllCaught();

        File exportDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "cobblemon_exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File jsonFile = null;
        File csvFile = null;

        if (exportJson) {
            jsonFile = new File(exportDir, "pokedex_export_" + timestamp + ".json");
            PokedexJsonExporter.export(records, jsonFile);

            // Update fixed latest file
            File latestJson = new File(exportDir, "pokedex_export_latest.json");
            PokedexJsonExporter.export(records, latestJson);
        }

        if (exportCsv) {
            csvFile = new File(exportDir, "pokedex_export_" + timestamp + ".csv");
            PokedexCsvExporter.export(records, csvFile);

            // Update fixed latest file
            File latestCsv = new File(exportDir, "pokedex_export_latest.csv");
            PokedexCsvExporter.export(records, latestCsv);
        }

        int uniqueSpecies = (int) records.stream().map(PokedexExportRecord::species).distinct().count();

        return new PokedexExportResult(records.size(), uniqueSpecies, jsonFile, csvFile);
    }

    private static PokedexExportRecord buildRecord(
            ResourceLocation speciesId,
            Species species,
            FormData formData,
            FormDexRecord fr,
            SpeciesDexRecord speciesRecord,
            String formName
    ) {
        String speciesName = species != null ? species.getName() : speciesId.getPath();
        int dexNumber = species != null ? species.getNationalPokedexNumber() : 0;
        String displayName = (species != null && species.getTranslatedName() != null)
                ? species.getTranslatedName().getString()
                : speciesName;

        // Form name normalization
        String resolvedForm = (formName == null || formName.isBlank()) ? "Normal" : formName;

        // Always labeled as CAUGHT
        String status = "CAUGHT";

        // Shiny caught status
        boolean caughtShiny = false;
        if (fr != null) {
            try {
                caughtShiny = fr.hasSeenShinyState(true);
            } catch (Throwable ignored) {}
        }

        // Caught genders
        List<String> caughtGenders = new ArrayList<>();
        if (fr != null) {
            try {
                Set<Gender> genders = fr.getGenders();
                if (genders != null && !genders.isEmpty()) {
                    for (Gender g : genders) {
                        if (g != null) caughtGenders.add(g.name());
                    }
                }
            } catch (Throwable ignored) {}
        }
        if (caughtGenders.isEmpty() && species != null) {
            if (species.getMaleRatio() == -1.0f) {
                caughtGenders.add("GENDERLESS");
            } else {
                caughtGenders.add("MALE/FEMALE");
            }
        }
        if (caughtGenders.isEmpty()) {
            caughtGenders.add("UNKNOWN");
        }

        // Highest level recorded in pokedex
        int highestLevel = 0;
        if (fr != null) {
            try {
                highestLevel = fr.getHighestLevel();
            } catch (Throwable ignored) {}
        }
        if (highestLevel <= 0 && speciesRecord != null) {
            try {
                highestLevel = speciesRecord.getHighestLevel();
            } catch (Throwable ignored) {}
        }

        // Elemental Types
        String primaryType = "NORMAL";
        if (formData != null && formData.getPrimaryType() != null) {
            primaryType = formData.getPrimaryType().getName().toUpperCase();
        } else if (species != null && species.getPrimaryType() != null) {
            primaryType = species.getPrimaryType().getName().toUpperCase();
        }

        String secondaryType = "None";
        if (formData != null && formData.getSecondaryType() != null) {
            secondaryType = formData.getSecondaryType().getName().toUpperCase();
        } else if (species != null && species.getSecondaryType() != null) {
            secondaryType = species.getSecondaryType().getName().toUpperCase();
        }

        // Base Stats
        int hp = getBaseStat(formData, species, Stats.HP);
        int atk = getBaseStat(formData, species, Stats.ATTACK);
        int def = getBaseStat(formData, species, Stats.DEFENCE);
        int spAtk = getBaseStat(formData, species, Stats.SPECIAL_ATTACK);
        int spDef = getBaseStat(formData, species, Stats.SPECIAL_DEFENCE);
        int speed = getBaseStat(formData, species, Stats.SPEED);
        int totalBaseStats = hp + atk + def + spAtk + spDef + speed;

        // Abilities
        List<String> abilities = new ArrayList<>();
        AbilityPool pool = formData != null && formData.getAbilities() != null
                ? formData.getAbilities()
                : (species != null ? species.getAbilities() : null);

        if (pool != null) {
            for (PotentialAbility pa : pool) {
                if (pa != null && pa.getTemplate() != null) {
                    String abilityName = pa.getTemplate().getName();
                    if (pa.getPriority() == Priority.LOW) {
                        abilityName += " (Hidden)";
                    }
                    if (!abilities.contains(abilityName)) {
                        abilities.add(abilityName);
                    }
                }
            }
        }
        if (abilities.isEmpty()) {
            abilities.add("None");
        }

        // Egg Groups
        List<String> eggGroupNames = new ArrayList<>();
        Set<EggGroup> eggGroups = formData != null ? formData.getEggGroups() : null;
        if ((eggGroups == null || eggGroups.isEmpty()) && species != null) {
            eggGroups = species.getEggGroups();
        }
        if (eggGroups != null) {
            for (EggGroup eg : eggGroups) {
                if (eg != null) eggGroupNames.add(eg.name());
            }
        }
        if (eggGroupNames.isEmpty()) {
            eggGroupNames.add("Undiscovered");
        }

        // Attributes
        int catchRate = formData != null ? formData.getCatchRate() : (species != null ? species.getCatchRate() : 0);
        float maleRatio = formData != null ? formData.getMaleRatio() : (species != null ? species.getMaleRatio() : -1.0f);
        int baseExp = formData != null ? formData.getBaseExperienceYield() : (species != null ? species.getBaseExperienceYield() : 0);
        int baseFriendship = formData != null ? formData.getBaseFriendship() : (species != null ? species.getBaseFriendship() : 0);
        float height = formData != null ? formData.getHeight() : (species != null ? species.getHeight() : 0.0f);
        float weight = formData != null ? formData.getWeight() : (species != null ? species.getWeight() : 0.0f);

        // Labels
        Set<String> labelSet = formData != null && formData.getLabels() != null
                ? formData.getLabels()
                : (species != null ? species.getLabels() : null);
        List<String> labels = labelSet != null ? new ArrayList<>(labelSet) : new ArrayList<>();

        // Aspects recorded in Pokédex
        List<String> aspects = new ArrayList<>();
        if (speciesRecord != null) {
            try {
                Set<String> aspectSet = speciesRecord.getAspects();
                if (aspectSet != null) {
                    aspects.addAll(aspectSet);
                }
            } catch (Throwable ignored) {}
        }

        return new PokedexExportRecord(
                dexNumber,
                speciesName,
                speciesId.toString(),
                resolvedForm,
                displayName,
                status,
                caughtShiny,
                caughtGenders,
                highestLevel,
                primaryType,
                secondaryType,
                hp,
                atk,
                def,
                spAtk,
                spDef,
                speed,
                totalBaseStats,
                abilities,
                eggGroupNames,
                catchRate,
                maleRatio,
                baseExp,
                baseFriendship,
                height,
                weight,
                labels,
                aspects
        );
    }

    private static String resolveFormDisplayName(String formKey, FormData formData, FormDexRecord fr) {
        if (formData != null && formData.getName() != null && !formData.getName().isBlank()) {
            return formData.getName();
        }
        if (formKey != null && !formKey.isBlank()) {
            return capitalize(formKey);
        }
        if (fr != null) {
            try {
                String fn = fr.getFormName();
                if (fn != null && !fn.isBlank()) {
                    return capitalize(fn);
                }
            } catch (Throwable ignored) {}
        }
        return "Normal";
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }

    private static int getBaseStat(FormData form, Species species, Stat stat) {
        if (form != null && form.getBaseStats() != null && form.getBaseStats().containsKey(stat)) {
            Integer val = form.getBaseStats().get(stat);
            if (val != null) return val;
        }
        if (species != null && species.getBaseStats() != null && species.getBaseStats().containsKey(stat)) {
            Integer val = species.getBaseStats().get(stat);
            if (val != null) return val;
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, FormDexRecord> extractFormRecords(SpeciesDexRecord record, ResourceLocation speciesId) {
        if (FORM_RECORDS_FIELD != null && record != null) {
            try {
                Map<String, FormDexRecord> map = (Map<String, FormDexRecord>) FORM_RECORDS_FIELD.get(record);
                if (map != null && !map.isEmpty()) {
                    return map;
                }
            } catch (Throwable ignored) {}
        }

        // Fallback: query each known form from species
        Map<String, FormDexRecord> fallbackMap = new HashMap<>();
        if (speciesId != null && record != null) {
            try {
                Species sp = PokemonSpecies.getByIdentifier(speciesId);
                if (sp != null) {
                    if (sp.getStandardForm() != null) {
                        String stdName = sp.getStandardForm().getName();
                        try {
                            FormDexRecord f = record.getFormRecord(stdName);
                            if (f != null) fallbackMap.put(stdName, f);
                        } catch (Throwable ignored) {}
                    }
                    if (sp.getForms() != null) {
                        for (FormData fd : sp.getForms()) {
                            if (fd != null && fd.getName() != null) {
                                try {
                                    FormDexRecord f = record.getFormRecord(fd.getName());
                                    if (f != null) fallbackMap.put(fd.getName(), f);
                                } catch (Throwable ignored) {}
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return fallbackMap;
    }
}
