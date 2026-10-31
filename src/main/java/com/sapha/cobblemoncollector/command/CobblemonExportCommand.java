package com.sapha.cobblemoncollector.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sapha.cobblemoncollector.collector.PokedexDataCollector;
import com.sapha.cobblemoncollector.collector.PokemonDataCollector;
import com.sapha.cobblemoncollector.model.PokedexExportRecord;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.io.File;
import java.util.List;

public class CobblemonExportCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(CobblemonExportCommand::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        // 1. PC & Party Data Commands (/collectpc and /cobbleexport)
        var collectPcNode = ClientCommandManager.literal("collectpc")
                .executes(ctx -> executeExport(ctx, true, true))
                .then(ClientCommandManager.literal("all").executes(ctx -> executeExport(ctx, true, true)))
                .then(ClientCommandManager.literal("json").executes(ctx -> executeExport(ctx, true, false)))
                .then(ClientCommandManager.literal("csv").executes(ctx -> executeExport(ctx, false, true)))
                .then(ClientCommandManager.literal("status").executes(CobblemonExportCommand::executeStatus));

        var cobbleExportNode = ClientCommandManager.literal("cobbleexport")
                .executes(ctx -> executeExport(ctx, true, true))
                .then(ClientCommandManager.literal("all").executes(ctx -> executeExport(ctx, true, true)))
                .then(ClientCommandManager.literal("json").executes(ctx -> executeExport(ctx, true, false)))
                .then(ClientCommandManager.literal("csv").executes(ctx -> executeExport(ctx, false, true)))
                .then(ClientCommandManager.literal("status").executes(CobblemonExportCommand::executeStatus));

        // 2. Pokédex CAUGHT Pokémon Commands (/collectdex and /cobbledex)
        var collectDexNode = ClientCommandManager.literal("collectdex")
                .executes(ctx -> executePokedexExport(ctx, true, true))
                .then(ClientCommandManager.literal("all").executes(ctx -> executePokedexExport(ctx, true, true)))
                .then(ClientCommandManager.literal("json").executes(ctx -> executePokedexExport(ctx, true, false)))
                .then(ClientCommandManager.literal("csv").executes(ctx -> executePokedexExport(ctx, false, true)))
                .then(ClientCommandManager.literal("status").executes(CobblemonExportCommand::executePokedexStatus));

        var cobbleDexNode = ClientCommandManager.literal("cobbledex")
                .executes(ctx -> executePokedexExport(ctx, true, true))
                .then(ClientCommandManager.literal("all").executes(ctx -> executePokedexExport(ctx, true, true)))
                .then(ClientCommandManager.literal("json").executes(ctx -> executePokedexExport(ctx, true, false)))
                .then(ClientCommandManager.literal("csv").executes(ctx -> executePokedexExport(ctx, false, true)))
                .then(ClientCommandManager.literal("status").executes(CobblemonExportCommand::executePokedexStatus));

        dispatcher.register(collectPcNode);
        dispatcher.register(cobbleExportNode);
        dispatcher.register(collectDexNode);
        dispatcher.register(cobbleDexNode);
    }

    public static int executeExport(CommandContext<FabricClientCommandSource> context, boolean exportJson, boolean exportCsv) {
        FabricClientCommandSource source = context.getSource();

        source.sendFeedback(Component.literal("§6[Cobblemon Collector] §fCollecting Pokémon data from memory..."));

        try {
            PokemonDataCollector.ExportResult result = PokemonDataCollector.exportData(exportJson, exportCsv);

            if (result.totalCount() == 0) {
                source.sendFeedback(Component.literal("§e[Cobblemon Collector] §cNo Pokémon found in memory! If you're on a server, please open your PC or Pasture once so the server syncs your boxes.")
                        .withStyle(ChatFormatting.YELLOW));
                return 0;
            }

            source.sendFeedback(Component.literal(String.format("§a✔ Export Successful! Collected §e%d §aPokémon (§e%d §aParty, §e%d §aPC across %d boxes).",
                    result.totalCount(), result.partyCount(), result.pcCount(), result.boxCount())));

            if (result.pcCount() == 0 && result.partyCount() > 0) {
                source.sendFeedback(Component.literal("§7Note: 0 PC Pokémon were exported. If you have Pokémon in PC boxes, open your PC block once in this session so the server syncs them, then re-run /collectpc!")
                        .withStyle(ChatFormatting.GRAY));
            }

            // Create clickable links for exported files
            if (result.csvFile() != null) {
                source.sendFeedback(createFileClickable("CSV Export", result.csvFile()));
            }
            if (result.jsonFile() != null) {
                source.sendFeedback(createFileClickable("JSON Export", result.jsonFile()));
            }

            // Folder link
            File folder = result.csvFile() != null ? result.csvFile().getParentFile() : (result.jsonFile() != null ? result.jsonFile().getParentFile() : null);
            if (folder != null) {
                source.sendFeedback(createFolderClickable(folder));
            }

            return 1;
        } catch (Exception e) {
            source.sendError(Component.literal("§c[Cobblemon Collector] Failed to export data: " + e.getMessage()));
            e.printStackTrace();
            return -1;
        }
    }

    public static int executePokedexExport(CommandContext<FabricClientCommandSource> context, boolean exportJson, boolean exportCsv) {
        FabricClientCommandSource source = context.getSource();

        source.sendFeedback(Component.literal("§6[Cobblemon Collector] §fCollecting CAUGHT Pokémon data from Pokédex..."));

        try {
            PokedexDataCollector.PokedexExportResult result = PokedexDataCollector.exportData(exportJson, exportCsv);

            if (result.totalCount() == 0) {
                source.sendFeedback(Component.literal("§e[Cobblemon Collector] §cNo caught Pokémon found in your Pokédex! Make sure you have caught Pokémon and that your Pokédex has synced.")
                        .withStyle(ChatFormatting.YELLOW));
                return 0;
            }

            source.sendFeedback(Component.literal(String.format("§a✔ Pokédex Export Successful! Collected §e%d §aCAUGHT Pokémon entries across §e%d §aunique species.",
                    result.totalCount(), result.uniqueSpeciesCount())));

            // Clickable file links
            if (result.csvFile() != null) {
                source.sendFeedback(createFileClickable("Pokédex CSV", result.csvFile()));
            }
            if (result.jsonFile() != null) {
                source.sendFeedback(createFileClickable("Pokédex JSON", result.jsonFile()));
            }

            // Folder link
            File folder = result.csvFile() != null ? result.csvFile().getParentFile() : (result.jsonFile() != null ? result.jsonFile().getParentFile() : null);
            if (folder != null) {
                source.sendFeedback(createFolderClickable(folder));
            }

            return 1;
        } catch (Exception e) {
            source.sendError(Component.literal("§c[Cobblemon Collector] Failed to export Pokédex data: " + e.getMessage()));
            e.printStackTrace();
            return -1;
        }
    }

    private static int executeStatus(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        var records = PokemonDataCollector.collectAll();
        long partyCount = records.stream().filter(r -> "PARTY".equalsIgnoreCase(r.source())).count();
        long pcCount = records.stream().filter(r -> "PC".equalsIgnoreCase(r.source())).count();

        source.sendFeedback(Component.literal("§6=== Cobblemon Collector Status ==="));
        source.sendFeedback(Component.literal(String.format("§fTotal Pokémon in memory: §e%d", records.size())));
        source.sendFeedback(Component.literal(String.format("§fParty Pokémon: §e%d", partyCount)));
        source.sendFeedback(Component.literal(String.format("§fPC Pokémon: §e%d", pcCount)));

        if (pcCount == 0) {
            source.sendFeedback(Component.literal("§7Tip: Open your PC block or Pasture once so the server streams your boxes to your client.")
                    .withStyle(ChatFormatting.GRAY));
        }

        return 1;
    }

    private static int executePokedexStatus(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        List<PokedexExportRecord> caughtRecords = PokedexDataCollector.collectAllCaught();
        long uniqueSpecies = caughtRecords.stream().map(PokedexExportRecord::species).distinct().count();
        long shinyCount = caughtRecords.stream().filter(PokedexExportRecord::caughtShiny).count();

        source.sendFeedback(Component.literal("§6=== Cobblemon Pokédex Caught Status ==="));
        source.sendFeedback(Component.literal(String.format("§fTotal Caught Pokémon Entries: §e%d", caughtRecords.size())));
        source.sendFeedback(Component.literal(String.format("§fUnique Species Caught: §e%d", uniqueSpecies)));
        source.sendFeedback(Component.literal(String.format("§fCaught Shiny Entries: §e%d", shinyCount)));

        if (caughtRecords.isEmpty()) {
            source.sendFeedback(Component.literal("§7Tip: Catch Pokémon in the wild or open your Pokédex to ensure records are synced.")
                    .withStyle(ChatFormatting.GRAY));
        }

        return 1;
    }

    private static MutableComponent createFileClickable(String label, File file) {
        return Component.literal(String.format(" §e• %s: §f%s §b[Open File]", label, file.getName()))
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open file: " + file.getAbsolutePath())))
                        .withUnderlined(false)
                );
    }

    private static MutableComponent createFolderClickable(File folder) {
        return Component.literal("§b[Open Exports Folder]")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, folder.getAbsolutePath()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open: " + folder.getAbsolutePath())))
                        .withUnderlined(true)
                        .withColor(ChatFormatting.AQUA)
                );
    }
}
