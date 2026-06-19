package dev.darkxx.kitsx.utils;

import dev.darkxx.kitsx.KitsX;
import dev.darkxx.kitsx.api.PremadeKitAPI;
import dev.darkxx.kitsx.utils.config.ConfigManager;
import dev.darkxx.kitsx.utils.editor.KitEditorSession;
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PremadeKitUtil implements PremadeKitAPI {

    private static ConfigManager configManager;
    private final Logger logger = Logger.getLogger(PremadeKitUtil.class.getName());
    private final Map<String, Long> lastBroadcastTime = new HashMap<>();

    public PremadeKitUtil(ConfigManager configManager) {
        PremadeKitUtil.configManager = configManager;
    }

    public static void of(KitsX plugin) {
        configManager = ConfigManager.get(plugin);
        configManager.create("data/premadekit.yml");
    }

    @Override
    public void save(Player player, String kitName) {
        try {
            InventorySnapshot snapshot = InventorySnapshot.fromPlayer(player);
            List<ItemStack> inventoryList = Arrays.asList(snapshot.getStorageContents());
            List<ItemStack> armorList = Arrays.asList(snapshot.getArmorContents());

            ConfigurationSection kitSection = configManager.getConfig("data/premadekit.yml").createSection("kits." + kitName);
            kitSection.set("inventory", inventoryList);
            kitSection.set("armor", armorList);
            kitSection.set("offhand", snapshot.getOffhandItem());

            configManager.saveConfig("data/premadekit.yml");

            String saved = KitsX.getInstance().getConfig().getString("messages.premade_kit_saved");
            if (saved != null) {
                player.sendMessage(ColorizeText.hex(saved.replace("%kit%", kitName)));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save kit: " + kitName, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(GuiBuilder inventory, String kitName) {
        InventorySnapshot snapshot = getSnapshot(kitName);
        if (snapshot != null) {
            snapshot.applyToGui(inventory);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public void load(Player player, String kitName) {
        if (KitEditorSessionManager.isEditing(player)) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6Finish your kit edit with /k# save or /kitcancel before loading premade kits."));
            return;
        }

        InventorySnapshot snapshot = getSnapshot(kitName);
        if (snapshot != null) {
            snapshot.applyToPlayer(player);
            long currentTime = System.currentTimeMillis();
            String playerName = player.getUniqueId().toString();
            long lastTime = lastBroadcastTime.getOrDefault(playerName, 0L);
            long delayMillis = KitsX.getInstance().getConfig().getInt("broadcast.premadekit_load_message_delay", 10) * 50L;

            if (KitsX.getInstance().getConfig().getBoolean("broadcast.premadekit_load", true) && (currentTime - lastTime > delayMillis)) {
                String bcastLoaded = KitsX.getInstance().getConfig().getString("broadcast.premadekit_load_message");
                if (bcastLoaded != null) {
                    bcastLoaded = bcastLoaded.replace("%player%", player.getName()).replace("%kit%", kitName);
                    Bukkit.broadcastMessage(ColorizeText.hex(bcastLoaded));
                }
                lastBroadcastTime.put(playerName, currentTime);
            }

            String loaded = KitsX.getInstance().getConfig().getString("messages.premade_kit_loaded");
            if (loaded != null) {
                player.sendMessage(ColorizeText.hex(loaded.replace("%kit%", kitName)));
            }

        } else {
            String empty = KitsX.getInstance().getConfig().getString("messages.premade_kit_empty");
            if (empty != null) {
                player.sendMessage(ColorizeText.hex(empty.replace("%kit%", kitName)));
            }
        }
    }

    public boolean loadIntoSession(Player player, String kitName) {
        player.sendMessage(ColorizeText.hex("&#ffa6a6Premade kits cannot be imported into an active editor. Use /customkit for the editor palette."));
        return false;
    }

    @SuppressWarnings("unchecked")
    private InventorySnapshot getSnapshot(String kitName) {
        ConfigurationSection kitSection = configManager.getConfig("data/premadekit.yml").getConfigurationSection("kits." + kitName);
        if (kitSection == null) {
            return null;
        }

        List<ItemStack> inventoryItems = (List<ItemStack>) kitSection.get("inventory");
        List<ItemStack> armorItems = (List<ItemStack>) kitSection.get("armor");
        ItemStack[] storage = new ItemStack[InventorySnapshot.STORAGE_SIZE];
        ItemStack[] armor = new ItemStack[InventorySnapshot.ARMOR_SIZE];

        if (inventoryItems != null) {
            for (int i = 0; i < Math.min(storage.length, inventoryItems.size()); i++) {
                storage[i] = cloneItem(inventoryItems.get(i));
            }
        }
        if (armorItems != null) {
            for (int i = 0; i < Math.min(armor.length, armorItems.size()); i++) {
                armor[i] = cloneItem(armorItems.get(i));
            }
        }
        return InventorySnapshot.fromArrays(storage, armor, kitSection.getItemStack("offhand"));
    }

    private ItemStack cloneItem(ItemStack item) {
        return item == null ? null : item.clone();
    }

    @Override
    public void saveAll() {
        try {
            configManager.saveConfig("data/premadekit.yml");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save premade kits file", e);
        }
    }
}
