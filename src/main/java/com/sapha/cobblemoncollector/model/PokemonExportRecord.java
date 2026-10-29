package com.sapha.cobblemoncollector.model;

import java.util.List;

/**
 * Record holding all collected Pokémon attributes for export.
 */
public record PokemonExportRecord(
        String source,          // "PARTY" or "PC"
        int boxNumber,          // 1-indexed (e.g. Box 1, Box 2) or 0 for Party
        int slotNumber,         // 1-indexed (1 to 30 for PC, 1 to 6 for Party)
        String species,         // Species name e.g. "Pikachu"
        String form,            // Form name e.g. "Normal" or "Hisuian"
        String displayName,     // Display Name or Nickname
        String gender,          // "MALE", "FEMALE", "GENDERLESS"
        int level,
        boolean isShiny,
        String ability,
        String nature,
        int hpIV,
        int atkIV,
        int defIV,
        int spAtkIV,
        int spDefIV,
        int speedIV,
        double totalIVPercentage,
        int perfectIVCount,
        List<String> eggGroups, // e.g. ["Field", "Fairy"]
        List<String> moves,
        String heldItem,
        String uuid
) {}
