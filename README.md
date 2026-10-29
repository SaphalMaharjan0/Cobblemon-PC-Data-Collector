# Cobblemon PC Data Collector (Client-Side)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?logo=minecraft)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Modloader-Fabric-blue.svg)](https://fabricmc.net/)
[![Cobblemon](https://img.shields.io/badge/Cobblemon-1.8.4+-red.svg)](https://cobblemon.com/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/SaphalMaharjan0/Cobblemon-PC-Data-Collector?color=success&label=Download%20Mod)](https://github.com/SaphalMaharjan0/Cobblemon-PC-Data-Collector/releases)

A dedicated, lightweight client-side Minecraft mod for **Cobblemon** (Minecraft **1.21.1 Fabric**) that automatically gathers and exports detailed Pokémon data directly from your client memory into **JSON** and **CSV** spreadsheets.

---

## 📥 Download & Installation

### 1. Download
Grab the latest compiled mod `.jar` from the GitHub Releases page:
- 📦 **[Download Latest Release (`cobblemon-pc-data-collector-1.0.0.jar`)](https://github.com/SaphalMaharjan0/Cobblemon-PC-Data-Collector/releases)**

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
3. Place `cobblemon-pc-data-collector-1.0.0.jar` into your `.minecraft/mods` directory.
4. Launch Minecraft, join any singleplayer world or multiplayer server, and start exporting!

---

## 🌟 Key Features

- **Purely Client-Side**: Safe to use on any multiplayer Cobblemon server. **No server-side installation or permission is required!**
- **Comprehensive Pokémon Details**:
  - **Species & Form**: Species name (e.g. `Pikachu`, `Vulpix`), Form (e.g. `Normal`, `Alolan`, `Hisuian`).
  - **Display Name / Nickname**: Preserves custom nicknames.
  - **Gender**: `MALE`, `FEMALE`, or `GENDERLESS`.
  - **Level & Shiny Status**: Current level and whether the Pokémon is shiny.
  - **Ability & Nature**: Ability name and effective Nature.
  - **Individual Values (IVs)**:
    - HP, Attack, Defense, Special Attack, Special Defense, Speed.
    - Total IV Percentage (0% to 100%).
    - Perfect IV Counter (e.g., count of 31s: 3x31, 5x31, 6x31).
  - **Breeding Egg Groups**: All assigned egg groups (e.g., `Field`, `Monster`, `Dragon`, `Fairy`) for quick breeding lookups.
  - **Moveset**: Currently equipped active moves.
  - **Storage Location**: Storage type (`PARTY` vs `PC`), Box number, and slot number.
  - **Traceability**: Unique UUID and equipped held item.
- **Dual Export Formats**:
  - **CSV (`.csv`)**: Spreadsheet-ready for Microsoft Excel, Google Sheets, or LibreOffice Calc. Perfect for breeding checklists, IV ranking, sorting, and trading.
  - **JSON (`.json`)**: Formatted data structure for web tools, Discord bots, and custom scripts.
- **Interactive Chat Feedback**: In-game clickable chat links that immediately open the exported file or destination folder.
- **Dedicated Hotkey**: Press **`P`** (customizable in Controls) for instant one-click export.

---

## 🎮 How to Use

### In-Game Commands
Type either `/collectpc` or `/cobbleexport` in chat:

| Command | Action |
|---|---|
| `/collectpc` | Exports both JSON and CSV files (Party + synced PC boxes) |
| `/collectpc all` | Explicitly exports all data to both JSON and CSV |
| `/collectpc csv` | Exports to CSV spreadsheet only |
| `/collectpc json` | Exports to JSON only |
| `/collectpc status` | Displays how many Party and PC Pokémon are currently loaded in memory |
| `/cobbleexport` | Alias for `/collectpc` |

### Hotkey
- Default Key: **`P`**
- You can rebind this key at any time in **Options** ➔ **Controls** ➔ **Key Binds** under the **"Cobblemon PC Collector"** category.

---

## 💡 Important Note: Server Sync Mechanism

In Cobblemon multiplayer, **PC box data is stored server-side** and streamed to your client:
1. **Active Party** is always available in memory immediately upon joining.
2. **PC Boxes** are streamed from the server when you open a **PC Block** or **Pasture**.
3. **Best Practice**: Whenever you join a server, simply interact with a PC block once. The server immediately syncs all your boxes to your client. You can then run `/collectpc` or press `P` anytime!

---

## 📂 Exported Files Location

All exports are saved into the `cobblemon_exports` folder inside your Minecraft directory:
```
.minecraft/
└── cobblemon_exports/
    ├── pokemon_export_2026-10-27_23-45-00.csv
    ├── pokemon_export_2026-10-27_23-45-00.json
    ├── pokemon_export_latest.csv   <-- Always points to your latest export!
    └── pokemon_export_latest.json  <-- Always points to your latest export!
```

### CSV Columns Exported
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
`build/libs/cobblemon-pc-data-collector-1.0.0.jar`

---

## 📜 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

Developed with ❤️ by [Saphal Maharjan](https://github.com/SaphalMaharjan0).
