package com.sapha.cobblemoncollector;

import com.sapha.cobblemoncollector.collector.PokedexDataCollector;
import com.sapha.cobblemoncollector.collector.PokemonDataCollector;
import com.sapha.cobblemoncollector.command.CobblemonExportCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CobblemonPCCollectorClient implements ClientModInitializer {

    public static final String MOD_ID = "cobblemon_pc_collector";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping exportKeyBinding;
    private static KeyMapping exportDexKeyBinding;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Cobblemon PC Collector] Initializing client-side data collector (v2.0.0)...");

        // 1. Register Client Commands (/collectpc, /cobbleexport, /collectdex, /cobbledex)
        CobblemonExportCommand.register();

        // 2. Register Hotkeys (P for PC, O for Pokédex)
        exportKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cobblemon_pc_collector.export",
                GLFW.GLFW_KEY_P,
                "category.cobblemon_pc_collector"
        ));

        exportDexKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cobblemon_pc_collector.export_pokedex",
                GLFW.GLFW_KEY_O,
                "category.cobblemon_pc_collector"
        ));

        // 3. Listen for Keybinding presses in client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (exportKeyBinding.consumeClick()) {
                if (client.player != null) {
                    performQuickExport(client);
                }
            }
            while (exportDexKeyBinding.consumeClick()) {
                if (client.player != null) {
                    performQuickPokedexExport(client);
                }
            }
        });

        LOGGER.info("[Cobblemon PC Collector] Successfully loaded! Use /collectpc, /collectdex, or press 'P' / 'O' to export data.");
    }

    private void performQuickExport(Minecraft client) {
        if (client.player == null) return;

        client.player.displayClientMessage(
                Component.literal("§6[Cobblemon Collector] §fExporting Pokémon data (JSON & CSV)..."),
                false
        );

        try {
            PokemonDataCollector.ExportResult result = PokemonDataCollector.exportData(true, true);

            if (result.totalCount() == 0) {
                client.player.displayClientMessage(
                        Component.literal("§e[Cobblemon Collector] §cNo Pokémon in memory yet! Please open your PC or Pasture once so your server sends your boxes.")
                                .withStyle(ChatFormatting.YELLOW),
                        false
                );
                return;
            }

            client.player.displayClientMessage(
                    Component.literal(String.format("§a✔ Exported §e%d §aPokémon (§e%d §aParty, §e%d §aPC) to CSV and JSON!",
                            result.totalCount(), result.partyCount(), result.pcCount())),
                    false
            );

            if (result.csvFile() != null) {
                File folder = result.csvFile().getParentFile();
                MutableComponent folderLink = Component.literal("§b[Click here to open folder: " + folder.getName() + "]")
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, folder.getAbsolutePath()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Open: " + folder.getAbsolutePath())))
                                .withUnderlined(true)
                                .withColor(ChatFormatting.AQUA)
                        );
                client.player.displayClientMessage(folderLink, false);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to export Pokémon data", e);
            client.player.displayClientMessage(
                    Component.literal("§c[Cobblemon Collector] Export failed: " + e.getMessage()),
                    false
            );
        }
    }

    private void performQuickPokedexExport(Minecraft client) {
        if (client.player == null) return;

        client.player.displayClientMessage(
                Component.literal("§6[Cobblemon Collector] §fExporting CAUGHT Pokédex data (JSON & CSV)..."),
                false
        );

        try {
            PokedexDataCollector.PokedexExportResult result = PokedexDataCollector.exportData(true, true);

            if (result.totalCount() == 0) {
                client.player.displayClientMessage(
                        Component.literal("§e[Cobblemon Collector] §cNo caught Pokémon found in your Pokédex! Make sure your Pokédex has synced.")
                                .withStyle(ChatFormatting.YELLOW),
                        false
                );
                return;
            }

            client.player.displayClientMessage(
                    Component.literal(String.format("§a✔ Exported §e%d §aCAUGHT Pokémon entries across §e%d §aunique species to CSV and JSON!",
                            result.totalCount(), result.uniqueSpeciesCount())),
                    false
            );

            if (result.csvFile() != null) {
                File folder = result.csvFile().getParentFile();
                MutableComponent folderLink = Component.literal("§b[Click here to open folder: " + folder.getName() + "]")
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, folder.getAbsolutePath()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Open: " + folder.getAbsolutePath())))
                                .withUnderlined(true)
                                .withColor(ChatFormatting.AQUA)
                        );
                client.player.displayClientMessage(folderLink, false);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to export Pokédex data", e);
            client.player.displayClientMessage(
                    Component.literal("§c[Cobblemon Collector] Pokédex export failed: " + e.getMessage()),
                    false
            );
        }
    }
}
