/*
 * This file is part of KitsX
 *
 * KitsX
 * Copyright (c) 2024 XyrisPlugins
 *
 * KitsX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KitsX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package dev.darkxx.kitsx.utils;

import dev.darkxx.kitsx.KitsX;
import dev.darkxx.kitsx.api.KitsAPI;
import dev.darkxx.kitsx.api.events.KitLoadEvent;
import dev.darkxx.kitsx.api.events.KitSaveEvent;
import dev.darkxx.kitsx.utils.config.ConfigManager;
import dev.darkxx.kitsx.utils.editor.KitEditorSession;
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KitUtil implements KitsAPI {

    private static ConfigManager configManager;
    private final Logger logger = Logger.getLogger(KitUtil.class.getName());
    private final Map<UUID, Long> lastKitLoadBroadcastAt = new HashMap<>();
    private final Map<UUID, Long> kitLoadCooldowns = new HashMap<>();

    public KitUtil(ConfigManager configManager) {
        KitUtil.configManager = configManager;
    }

    public static void of(JavaPlugin plugin) {
        configManager = ConfigManager.get(plugin);
        configManager.create("data/kits.yml");
    }

    @Override
    public void save(@NotNull Player player, String kitName) {
        InventorySnapshot snapshot = InventorySnapshot.fromInventory(player.getOpenInventory().getTopInventory());
        saveSnapshot(player, kitName, snapshot.getStorageContents(), snapshot.getArmorContents(), snapshot.getOffhandItem());
    }

    public void saveSnapshot(@NotNull Player player,
                             @NotNull String kitName,
                             @NotNull ItemStack[] inventoryContents,
                             @NotNull ItemStack[] armorContents,
                             ItemStack offhandItem) {
        String playerName = player.getUniqueId().toString();

        ItemStack[] invCopy = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            invCopy[i] = cloneItem(i < inventoryContents.length ? inventoryContents[i] : null);
        }

        ItemStack[] armorCopy = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armorCopy[i] = cloneItem(i < armorContents.length ? armorContents[i] : null);
        }

        ItemStack offhandCopy = cloneItem(offhandItem);

        KitSaveEvent event = new KitSaveEvent(player, kitName, invCopy, armorCopy, offhandCopy);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }
        configManager.set("data/kits.yml", playerName + "." + kitName, null);

        Map<String, Object> kitData = new HashMap<>();
        for (int i = 0; i < 36; ++i) {
            kitData.put(playerName + "." + kitName + ".inventory." + i,
                cloneItem(i < event.getInventoryContents().length ? event.getInventoryContents()[i] : null));
        }
        for (int i = 0; i < 4; ++i) {
            kitData.put(playerName + "." + kitName + ".armor." + i,
                cloneItem(i < event.getArmorContents().length ? event.getArmorContents()[i] : null));
        }
        kitData.put(playerName + "." + kitName + ".offhand", cloneItem(event.getOffhandItem()));

        for (Map.Entry<String, Object> entry : kitData.entrySet()) {
            configManager.set("data/kits.yml", entry.getKey(), entry.getValue());
        }

        try {
            configManager.saveConfig("data/kits.yml");
            String kitSaved = Objects.requireNonNull(KitsX.getInstance().getConfig().getString("messages.kit_saved"))
                    .replace("%kit%", kitName);
            player.sendMessage(ColorizeText.hex(kitSaved));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save kit: " + kitName, e);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void load(@NotNull Player player, String kitName) {
        if (KitEditorSessionManager.isEditing(player)) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6Finish your kit edit with /k# save or /kitcancel before loading kits."));
            return;
        }

        long cooldownMillis = resolveKitLoadCooldownMillis();
        if (cooldownMillis > 0 && !player.hasPermission("kitsx.cooldown.bypass")) {
            long now = System.currentTimeMillis();
            long lastUse = kitLoadCooldowns.getOrDefault(player.getUniqueId(), 0L);
            long elapsed = now - lastUse;

            if (elapsed < cooldownMillis) {
                long remainingMillis = cooldownMillis - elapsed;
                String formatted = formatDuration(remainingMillis);
                String cooldownMsg = KitsX.getInstance().getConfig().getString("messages.kit_load_cooldown");
                if (cooldownMsg != null && !cooldownMsg.isEmpty()) {
                    player.sendMessage(ColorizeText.hex(cooldownMsg.replace("%time%", formatted)));
                }
                return;
            }
        }

        KitLoadEvent event = new KitLoadEvent(player, kitName);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            InventorySnapshot snapshot = getSnapshot(player, kitName);
            if (snapshot != null) {
                kitLoadCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                snapshot.applyToPlayer(player);
                sendKitLoadBroadcast(player, kitName);
                sendKitLoadedMessage(player, kitName);
            } else {
                player.sendMessage(ColorizeText.hex("&#ffa6a6" + kitName + " is empty."));
            }
        }
    }

    public void loadForEditor(@NotNull Player player, @NotNull String kitName) {
        InventorySnapshot snapshot = getSnapshot(player, kitName);
        if (snapshot == null) {
            snapshot = InventorySnapshot.empty();
        }
        snapshot.applyToPlayer(player);
    }

    public boolean loadIntoSession(@NotNull Player player, @NotNull String kitName) {
        player.sendMessage(ColorizeText.hex("&#ffa6a6Kit loads cannot be imported into an active editor. Use /customkit for the editor palette."));
        return false;
    }

    private InventorySnapshot getSnapshot(@NotNull Player player, @NotNull String kitName) {
        String playerName = player.getUniqueId().toString();
        if (!configManager.contains("data/kits.yml", playerName + "." + kitName)) {
            return null;
        }

        ItemStack[] inventoryContents = new ItemStack[36];
        for (int i = 0; i < 36; ++i) {
            inventoryContents[i] = configManager.getConfig("data/kits.yml")
                .getItemStack(playerName + "." + kitName + ".inventory." + i);
        }

        ItemStack[] armorContents = new ItemStack[4];
        for (int i = 0; i < 4; ++i) {
            armorContents[i] = configManager.getConfig("data/kits.yml")
                .getItemStack(playerName + "." + kitName + ".armor." + i);
        }

        ItemStack offhandItem = configManager.getConfig("data/kits.yml")
            .getItemStack(playerName + "." + kitName + ".offhand");
        return InventorySnapshot.fromArrays(inventoryContents, armorContents, offhandItem);
    }

    private void sendKitLoadBroadcast(@NotNull Player player, @NotNull String kitName) {
        long currentTime = System.currentTimeMillis();
        long lastTime = lastKitLoadBroadcastAt.getOrDefault(player.getUniqueId(), 0L);
        long delayMillis = KitsX.getInstance().getConfig().getInt("broadcast.kit_load_message_delay", 10) * 50L;

        if (!KitsX.getInstance().getConfig().getBoolean("broadcast.kit_load", true) || currentTime - lastTime <= delayMillis) {
            return;
        }

        List<String> lines = KitsX.getInstance().getConfig().getStringList("broadcast.kit_load_message");
        if (lines == null || lines.isEmpty()) {
            String raw = KitsX.getInstance().getConfig().getString("broadcast.kit_load_message");
            if (raw != null && !raw.isEmpty()) {
                raw = raw.replace("\\n", "\n");
                lines = Arrays.asList(raw.split("\\n", -1));
            }
        }

        if (lines == null || lines.isEmpty()) {
            return;
        }

        for (String line : lines) {
            if (line == null || line.isEmpty()) {
                continue;
            }
            String rendered = line.replace("%player%", player.getName()).replace("%kit%", kitName);
            Bukkit.broadcastMessage(ColorizeText.hex(rendered));
        }
        lastKitLoadBroadcastAt.put(player.getUniqueId(), currentTime);
    }

    private void sendKitLoadedMessage(@NotNull Player player, @NotNull String kitName) {
        String kitLoaded = KitsX.getInstance().getConfig().getString("messages.kit_loaded");
        if (kitLoaded != null) {
            player.sendMessage(ColorizeText.hex(kitLoaded.replace("%kit%", kitName)));
        }
    }

    public void resetCooldown(UUID uuid) {
        kitLoadCooldowns.remove(uuid);
    }

    public void resetAllCooldowns() {
        kitLoadCooldowns.clear();
    }

    private long resolveKitLoadCooldownMillis() {
        String raw = KitsX.getInstance().getConfig().getString("cooldowns.kit_load", "0");
        if (raw == null || raw.trim().isEmpty()) {
            long legacySeconds = KitsX.getInstance().getConfig().getLong("cooldowns.kit_load_seconds", 0);
            return legacySeconds <= 0 ? 0 : legacySeconds * 1000L;
        }
        String normalized = raw.trim();
        long parsed = parseDurationMillis(normalized);
        if (parsed > 0) {
            return parsed;
        }
        if (parsed == 0 && normalized.matches("0+[smhd]?")) {
            return 0;
        }
        long legacySeconds = KitsX.getInstance().getConfig().getLong("cooldowns.kit_load_seconds", 0);
        return legacySeconds <= 0 ? 0 : legacySeconds * 1000L;
    }

    public static long parseDurationMillis(String input) {
        long totalMillis = 0;
        String working = input.toLowerCase(Locale.ENGLISH).replaceAll("\\s+", "");
        int index = 0;
        while (index < working.length()) {
            int start = index;
            while (index < working.length() && Character.isDigit(working.charAt(index))) {
                index++;
            }
            if (start == index) {
                return -1;
            }
            long value = Long.parseLong(working.substring(start, index));
            if (index >= working.length()) {
                totalMillis += value * 1000L;
                break;
            }
            char unit = working.charAt(index++);
            switch (unit) {
                case 'd':
                    totalMillis += value * 86_400_000L;
                    break;
                case 'h':
                    totalMillis += value * 3_600_000L;
                    break;
                case 'm':
                    totalMillis += value * 60_000L;
                    break;
                case 's':
                    totalMillis += value * 1000L;
                    break;
                default:
                    return -1;
            }
        }
        return totalMillis;
    }

    public static String formatDuration(long millis) {
        if (millis < 1000) {
            return "1 second";
        }

        long totalSeconds = millis / 1000;
        long days = totalSeconds / 86_400;
        totalSeconds %= 86_400;
        long hours = totalSeconds / 3_600;
        totalSeconds %= 3_600;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(days + " day" + (days == 1 ? "" : "s"));
        }
        if (hours > 0) {
            parts.add(hours + " hour" + (hours == 1 ? "" : "s"));
        }
        if (minutes > 0) {
            parts.add(minutes + " minute" + (minutes == 1 ? "" : "s"));
        }

        if (parts.isEmpty()) {
            parts.add(seconds + " second" + (seconds == 1 ? "" : "s"));
        }

        return String.join(" ", parts);
    }

    @Override
    public void set(@NotNull Player player, String kitName, GuiBuilder inventory) {
        InventorySnapshot snapshot = getSnapshot(player, kitName);
        if (snapshot != null) {
            snapshot.applyToGui(inventory);
        }
    }

    @Override
    public void importInventory(@NotNull Player player, GuiBuilder inventory) {
        if (!KitEditorSessionManager.isEditing(player)) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6Inventory import is only available inside a kit edit session."));
            return;
        }
        InventorySnapshot.fromPlayer(player).applyToGui(inventory);
    }

    public void importFromSession(@NotNull GuiBuilder inventory, @NotNull KitEditorSession session) {
        // The original inventory snapshot is restoration-only and must never be imported into a kit.
    }

    @Override
    public void delete(@NotNull Player player, String kitName) {
        String playerName = player.getUniqueId().toString();

        configManager.set("data/kits.yml", playerName + "." + kitName, null);

        try {
            configManager.saveConfig("data/kits.yml");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete kit: " + kitName, e);
        }
    }

    @Override
    public boolean exists(@NotNull Player player, String kitName) {
        String playerName = player.getUniqueId().toString();
        return configManager.contains("data/kits.yml", playerName + "." + kitName);
    }

    @Override
    public void saveAll() {
        try {
            configManager.saveConfig("data/kits.yml");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save kits file", e);
        }
    }

    private ItemStack cloneItem(ItemStack stack) {
        return stack == null ? null : stack.clone();
    }
}
