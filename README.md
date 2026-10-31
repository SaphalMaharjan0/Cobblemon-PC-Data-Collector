# Cobblemon PC Data Collector (Client-Side)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?logo=minecraft)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Modloader-Fabric-blue.svg)](https://fabricmc.net/)
[![Cobblemon](https://img.shields.io/badge/Cobblemon-1.8.4+-red.svg)](https://cobblemon.com/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/SaphalMaharjan0/Cobblemon-PC-Data-Collector?color=success&label=Download%20Mod)](https://github.com/SaphalMaharjan0/Cobblemon-PC-Data-Collector/releases)

A dedicated, lightweight client-side Minecraft mod for **Cobblemon** (Minecraft **1.21.1 Fabric**) that automatically gathers and exports detailed Pokémon data from your **PC/Party storage** and your **Pokédex caught records** directly into clean, spreadsheet-ready **CSV** and structured **JSON** files.

---

## 🚀 What's New in Version 2.0.0

- 📖 **Pokédex Caught Data Collector (`/collectdex` & `/cobbledex`)**:
  - Automatically queries your client Pokédex to export every Pokémon registered as **`CAUGHT`** (`PokedexEntryProgress.OWNED`).
  - Strict filtering ignores unseen and seen-only Pokémon to provide a 100% verified caught checklist.
- 🧬 **Form & Regional Variant Resolution**:
  - Full awareness of regional variants (e.g., Alolan Vulpix, Galarian Ponyta, Hisuian Growlithe, Paldean Wooper) with individual caught and shiny status tracking.
- 📊 **28 Comprehensive Export Attributes**:
  - Captures National Dex Number, Species, Resource Identifier, Form, Display Name, Status (`CAUGHT`), Shiny Caught flag, Genders Caught, Highest Level recorded, Elemental Types, Base Stats (HP, Atk, Def, SpAtk, SpDef, Speed, BST), Standard & Hidden Abilities, Breeding Egg Groups, Catch Rate, Male Ratio, Base EXP, Base Friendship, Height, Weight, Labels, and Aspects.
- ⌨️ **Dedicated Hotkey (`O`)**:
  - Instant one-click Pokédex export using key **`O`** (customizable in Controls under *"Cobblemon PC Collector"*).
- 🛡️ **Multiplayer Server Resilience**:
  - Built-in packet decoding fail-safes protect against Kotlin uninitialized properties on server-streamed Pokédex data.
- 🔗 **Clickable Chat Links**:
  - Interactive in-game chat messages provide `[Open File]` and `[Open Exports Folder]` clickable shortcuts.

---

## 📥 Download & Installation

### 1. Download
Grab the latest compiled mod `.jar` from the GitHub Releases page:
- 📦 **[Download Latest Release (`cobblemon-pc-data-collector-2.0.0.jar`)](https://github.com/SaphalMaharjan0/Cobblemon-PC-Data-Collector/releases)**

### 2. Requirements
| Requirement | Recommended Version |
|---|---|
| **Minecraft** | `1.21.1` |
| **Fabric Loader** | `>= 0.16.0` |
| **Fabric API** | `0.115.0+1.21.1` (or latest 1.21.1) |
| **Cobblemon** | `1.8.4+` (Fabric) |
| **Java** | `21` |

### 3. Installation Steps
1. Make sure you have **Minecraft 1.21.1** installed with **Fabric Loader**.
2. Download the required **[Fabric API](https://modrinth.com/mod/fabric-api)** and **[Cobblemon](https://modrinth.com/mod/cobblemon)** jars into your `.minecraft/mods` folder.
3. Place `cobblemon-pc-data-collector-2.0.0.jar` into your `.minecraft/mods` directory.
4. Launch Minecraft, join any singleplayer world or multiplayer server, and start exporting!

---

## 🌟 Key Features

- **Purely Client-Side**: Safe to use on any multiplayer Cobblemon server. **No server-side mod installation or permission is required!**
- **Dual Collector Systems**:
  1. **PC & Party Collector (`/collectpc`)**: Complete inspection of your owned Pokémon in team slots and synced PC boxes (IVs, moves, items, nature, ability).
  2. **Pokédex Caught Collector (`/collectdex`)** *(New in v2.0.0)*: Queries your client Pokédex manager to export every Pokémon registered as **CAUGHT** (`OWNED`), including regional forms, shiny registry, highest level reached, base stats, types, abilities, and breeding egg groups.
- **Dual Export Formats**:
  - **CSV (`.csv`)**: Spreadsheet-ready for Microsoft Excel, Google Sheets, or LibreOffice Calc. Perfect for checklist tracking, IV sorting, living dex completion, and trading.
  - **JSON (`.json`)**: Formatted data structure for web tools, Discord bots, and custom applications.
- **Interactive Chat Feedback**: In-game clickable chat links that immediately open the exported file or destination folder.
- **Dedicated Hotkeys**:
  - Press **`P`** for instant PC & Party export.
  - Press **`O`** for instant Pokédex Caught export.

---

## 🎮 How to Use

### 1. Pokédex Caught Exports (`/collectdex`)
Type `/collectdex` or `/cobbledex` in chat to export all Pokémon registered as **CAUGHT** in your Pokédex:

| Command | Action |
|---|---|
| `/collectdex` | Exports both JSON and CSV files of all Pokémon registered as **CAUGHT** in your Pokédex |
| `/collectdex all` | Explicitly exports all caught Pokédex data to both JSON and CSV |
| `/collectdex csv` | Exports caught Pokédex records to CSV spreadsheet only |
| `/collectdex json` | Exports caught Pokédex records to JSON only |
| `/collectdex status` | Displays how many caught Pokémon are currently registered in your Pokédex |
| `/cobbledex` | Alias for `/collectdex` |

**Hotkey**: Default Key **`O`** (customizable in Controls under *"Cobblemon PC Collector"*).

### 2. PC & Party Storage Exports (`/collectpc`)
Type `/collectpc` or `/cobbleexport` in chat to export your active team and PC boxes:

| Command | Action |
|---|---|
| `/collectpc` | Exports both JSON and CSV files (Party + synced PC boxes) |
| `/collectpc all` | Explicitly exports all PC & Party data to both JSON and CSV |
| `/collectpc csv` | Exports PC & Party data to CSV spreadsheet only |
| `/collectpc json` | Exports PC & Party data to JSON only |
| `/collectpc status` | Displays how many Party and PC Pokémon are currently loaded in memory |
| `/cobbleexport` | Alias for `/collectpc` |

**Hotkey**: Default Key **`P`** (customizable in Controls under *"Cobblemon PC Collector"*).

---

## 💡 Important Note: Server Sync Mechanism

In Cobblemon multiplayer, data is streamed to your client:
1. **Pokédex Data**: Synced by the server upon joining and updated whenever Pokémon are caught or discovered.
2. **Active Party**: Always available in memory immediately upon joining.
3. **PC Boxes**: Streamed from the server when you open a **PC Block** or **Pasture**. Simply interact with a PC block once upon joining to load all box data into client memory!

---

## 📂 Exported Files Location

All exports are saved into the `cobblemon_exports` folder inside your Minecraft directory:
```
.minecraft/
└── cobblemon_exports/
    ├── pokedex_caught_export_2026-10-31_19-30-00.csv   <-- Pokédex Caught Export
    ├── pokedex_caught_export_2026-10-31_19-30-00.json
    ├── pokedex_caught_latest.csv                       <-- Quick link to latest Dex export
    ├── pokedex_caught_latest.json
    ├── pokemon_export_2026-10-31_19-30-00.csv          <-- PC/Party Export
    ├── pokemon_export_2026-10-31_19-30-00.json
    ├── pokemon_export_latest.csv                       <-- Quick link to latest PC export
    └── pokemon_export_latest.json
```

---

## 📊 Exported Fields

### Pokédex Caught Export (`/collectdex`)
Every exported entry is verified as **`CAUGHT`** (`PokedexEntryProgress.OWNED`):
- `DexNumber`: National Pokédex number (sorted ascending)
- `Species`: Species display name (e.g., `Pikachu`, `Vulpix`)
- `SpeciesIdentifier`: Cobblemon resource identifier (e.g., `cobblemon:pikachu`)
- `Form`: Specific form name (e.g., `Normal`, `Alolan`, `Galarian`, `Hisuian`, `Paldean`)
- `DisplayName`: Formatted species/form display title
- `Status`: Always **`CAUGHT`**
- `CaughtShiny`: `true` if shiny form has been unlocked/caught in Dex, `false` otherwise
- `CaughtGenders`: Genders registered as caught (`[MALE, FEMALE]`, `[GENDERLESS]`, etc.)
- `HighestLevel`: Highest level achieved/registered for this species form
- `PrimaryType` & `SecondaryType`: Elemental typings (e.g., `Electric`, `Ice / Fairy`)
- `BaseHP`, `BaseAttack`, `BaseDefense`, `BaseSpAtk`, `BaseSpDef`, `BaseSpeed`: Base stat attributes
- `BaseTotal`: Base Stat Total (BST)
- `Abilities`: All standard and hidden abilities for the species
- `EggGroups`: Breeding egg groups (e.g., `Field`, `Monster`, `Dragon`)
- `CatchRate`: Base species catch rate
- `MaleRatio`: Gender distribution percentage
- `BaseExperience` & `BaseFriendship`: Base EXP reward and starting friendship
- `Height` & `Weight`: Official dimensions
- `Labels`: Classification tags (e.g., `gen1`, `starter`, `legendary`, `pseudo_legendary`)
- `Aspects`: Active Cobblemon model aspects

### PC & Party Storage Export (`/collectpc`)
- `LocationType` (`PARTY` or `PC`)
- `BoxNumber` & `SlotNumber`
- `Species`, `Form`, `DisplayName`
- `Level`, `Gender`, `Shiny`
- `Nature`, `Ability`
- `HP_IV`, `Atk_IV`, `Def_IV`, `SpAtk_IV`, `SpDef_IV`, `Speed_IV`
- `TotalIV_Percent`, `Perfect_31_Count`
- `EggGroups`
- `HeldItem`
- `Move1`, `Move2`, `Move3`, `Move4`
- `UUID`

---

## 🛠️ Building From Source

This project uses standard **Fabric Loom** with **Java 21**:

```bash
# Clone the repository
git clone https://github.com/SaphalMaharjan0/Cobblemon-PC-Data-Collector.git
cd Cobblemon-PC-Data-Collector

# Build the mod jar using Gradle wrapper
# Windows:
.\gradlew.bat build

# Linux / macOS:
./gradlew build
```

The compiled mod jar will be in:
`build/libs/cobblemon-pc-data-collector-2.0.0.jar`

---

## 📜 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

Developed with ❤️ by [Saphal Maharjan](https://github.com/SaphalMaharjan0).
