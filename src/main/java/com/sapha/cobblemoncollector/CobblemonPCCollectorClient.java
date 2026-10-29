package com.sapha.cobblemoncollector;

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

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Cobblemon PC Collector] Initializing client-side data collector...");

        // 1. Register Client Commands (/collectpc and /cobbleexport)
        CobblemonExportCommand.register();

        // 2. Register Hotkey (default: KEY_P)
        exportKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cobblemon_pc_collector.export",
                GLFW.GLFW_KEY_P,
                "category.cobblemon_pc_collector"
        ));

        // 3. Listen for Keybinding presses in client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (exportKeyBinding.consumeClick()) {
                if (client.player != null) {
                    performQuickExport(client);
                }
            }
        });

        LOGGER.info("[Cobblemon PC Collector] Successfully loaded! Use /collectpc or press 'P' to export data.");
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
}
