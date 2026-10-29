package com.sapha.cobblemoncollector.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sapha.cobblemoncollector.collector.PokemonDataCollector;
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

public class CobblemonExportCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(CobblemonExportCommand::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
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

        dispatcher.register(collectPcNode);
        dispatcher.register(cobbleExportNode);
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
                MutableComponent folderLink = Component.literal("§b[Open Exports Folder]")
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, folder.getAbsolutePath()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open: " + folder.getAbsolutePath())))
                                .withUnderlined(true)
                                .withColor(ChatFormatting.AQUA)
                        );
                source.sendFeedback(folderLink);
            }

            return 1;
        } catch (Exception e) {
            source.sendError(Component.literal("§c[Cobblemon Collector] Failed to export data: " + e.getMessage()));
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

    private static MutableComponent createFileClickable(String label, File file) {
        return Component.literal(String.format(" §e• %s: §f%s §b[Open File]", label, file.getName()))
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to open file: " + file.getAbsolutePath())))
                        .withUnderlined(false)
                );
    }
}
