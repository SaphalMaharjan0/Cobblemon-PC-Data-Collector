package com.sapha.cobblemoncollector.model;

import java.util.List;

/**
 * Record holding all Pokédex attributes for an exported caught Pokémon.
 */
public record PokedexExportRecord(
        int dexNumber,              // National Pokédex number e.g. 1, 25
        String species,             // Species name e.g. "Pikachu", "Charizard"
        String speciesIdentifier,   // ResourceLocation string e.g. "cobblemon:pikachu"
        String form,                // Form name e.g. "Normal", "Alolan", "Hisuian"
        String displayName,         // Localized/Translated Pokémon name
        String status,              // Always "CAUGHT"
        boolean caughtShiny,        // Whether player caught/registered shiny form
        List<String> caughtGenders, // Genders registered e.g. ["MALE", "FEMALE"]
        int highestLevel,           // Highest level recorded in Pokédex
        String primaryType,         // Primary elemental type e.g. "ELECTRIC", "GRASS"
        String secondaryType,       // Secondary elemental type or "None"
        int baseHp,                 // Base HP
        int baseAttack,             // Base Attack
        int baseDefense,            // Base Defense
        int baseSpAttack,           // Base Sp. Attack
        int baseSpDefense,          // Base Sp. Defense
        int baseSpeed,              // Base Speed
        int totalBaseStats,         // Base stat total (BST)
        List<String> abilities,     // Available abilities
        List<String> eggGroups,     // Egg groups e.g. ["Field", "Fairy"]
        int catchRate,              // Catch rate e.g. 190
        float maleRatio,            // Male ratio percentage or -1 for genderless
        int baseExperience,         // Base EXP yield
        int baseFriendship,         // Base friendship
        float height,               // Height in meters
        float weight,               // Weight in kg
        List<String> labels,        // Labels e.g. ["gen1", "starter"]
        List<String> aspects        // Aspects recorded in Pokédex
) {}
