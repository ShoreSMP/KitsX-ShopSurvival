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

package dev.darkxx.kitsx.menus;

import dev.darkxx.kitsx.KitsX;
import dev.darkxx.kitsx.utils.config.MenuConfig;
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.kitsx.utils.wg.BlacklistedRegion;
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.menu.xmenu.ItemBuilderGUI;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class KitsMenu extends GuiBuilder {

    private static final KitsX PLUGIN = KitsX.getInstance();
    private static final MenuConfig CONFIG = new MenuConfig(PLUGIN, "menus/kits_menu.yml");
    private static final Logger LOGGER = PLUGIN.getLogger();

    public KitsMenu(int size) {
        super(size);
    }

    public static @NotNull GuiBuilder openKitMenu(Player player) {
        if (KitEditorSessionManager.isEditing(player)) {
            return KitRoomMenu.openKitRoom(player);
        }

        int inventorySize = CONFIG.getConfig().getInt("kits_menu.size", 54);
        String kitsTitle = CONFIG.getConfig().getString("kits_menu.title", "Kits");
        String inventoryTitle = ColorizeText.hex(kitsTitle);

        GuiBuilder inventory = new GuiBuilder(inventorySize, inventoryTitle);

        if (CONFIG.getConfig().getBoolean("kits_menu.filter.enabled", true)) {
            addFilterItems(inventory);
        }

        addKitItems(inventory, player);
        addKitRoomItem(inventory, player);
        addClearInventoryItem(inventory, player);

        return inventory;
    }

    private static void addFilterItems(GuiBuilder inventory) {
        String filterMaterial = CONFIG.getConfig().getString("kits_menu.filter.material", "BLACK_STAINED_GLASS_PANE");
        String filterName = CONFIG.getConfig().getString("kits_menu.filter.name", " ");
        List<String> filterFlagsList = CONFIG.getConfig().getStringList("kits_menu.filter.flags");
        List<ItemFlag> filterFlags = new ArrayList<>();
        for (String flag : filterFlagsList) {
            try {
                filterFlags.add(ItemFlag.valueOf(flag));
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid item flag " + flag);
            }
        }
        List<Integer> filterSlots = CONFIG.getConfig().getIntegerList("kits_menu.filter.slots");

        ItemStack filter = new ItemBuilderGUI(Material.valueOf(filterMaterial))
                .name(filterName)
                .flags(filterFlags.toArray(new ItemFlag[0]))
                .build();

        for (int slot : filterSlots) {
            inventory.setItem(slot, filter);
        }
    }

    public static void addKitItems(GuiBuilder inventory, Player player) {
        addItemGroup(inventory, "kits_menu.kits", Material.END_CRYSTAL, player);
    }

    public static void addItemGroup(GuiBuilder inventory, String configPath, Material defaultMaterial, Player player) {
        List<Integer> slots = CONFIG.getConfig().getIntegerList(configPath + ".slots");

        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            int kitNumber = i + 1;

            String itemMaterial = CONFIG.getConfig().getString(configPath + ".material", defaultMaterial.name());
            String itemName = CONFIG.getConfig().getString(configPath + ".name", "").replace("%kit%", String.valueOf(kitNumber));
            List<String> loreList = CONFIG.getConfig().getStringList(configPath + ".lore");
            List<String> finalLore = new ArrayList<>();
            for (String lore : loreList) {
                finalLore.add(ColorizeText.hex(lore.replace("%i%", String.valueOf(kitNumber))));
            }
            List<String> flagList = CONFIG.getConfig().getStringList(configPath + ".flags");
            List<ItemFlag> flags = new ArrayList<>();
            for (String flag : flagList) {
                try {
                    flags.add(ItemFlag.valueOf(flag));
                } catch (IllegalArgumentException e) {
                    LOGGER.warning("Invalid item flag " + flag);
                }
            }

            ItemStack item = new ItemBuilderGUI(Material.valueOf(itemMaterial))
                    .name(ColorizeText.hex(itemName))
                    .lore(finalLore.toArray(new String[0]))
                    .flags(flags.toArray(new ItemFlag[0]))
                    .build();

            if (!hasKitPermission(player, kitNumber)) {
                continue;
            }
            inventory.setItem(slot, item, e -> {
                Player clicker = (Player) e.getWhoClicked();
                if (e.isRightClick()) {
                    startEditor(clicker, kitNumber);
                } else if (e.isLeftClick()) {
                    if (KitEditorSessionManager.isEditing(clicker)) {
                        clicker.sendMessage(ColorizeText.hex("&#ffa6a6Finish the current edit with /k# save or /kitcancel first."));
                        return;
                    }
                    KitsX.getKitUtil().load(clicker, "Kit " + kitNumber);
                }
            });
        }
    }

    private static boolean hasKitPermission(Player player, int kitNumber) {
        return player.hasPermission("kitsx.kit" + kitNumber);
    }

    private static void startEditor(Player player, int kitNumber) {
        if (KitEditorSessionManager.isEditing(player)) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6Finish the current edit with /k# save or /kitcancel first."));
            return;
        }
        if (!BlacklistedRegion.isInEditorRegion(player)) {
            sendConfigMessage(player, "messages.editor_region_required", "&#ffa6a6You can only edit kits in the kit editor region.");
            return;
        }
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6Clear your cursor before editing a kit."));
            return;
        }

        player.closeInventory();
        PLUGIN.getServer().getScheduler().runTask(PLUGIN, () -> startEditorAfterInventorySettles(player, kitNumber));
    }

    private static void startEditorAfterInventorySettles(Player player, int kitNumber) {
        if (!player.isOnline()) {
            return;
        }
        if (KitEditorSessionManager.isEditing(player)) {
            return;
        }
        if (!BlacklistedRegion.isInEditorRegion(player)) {
            sendConfigMessage(player, "messages.editor_region_required", "&#ffa6a6You can only edit kits in the kit editor region.");
            return;
        }
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6Clear your cursor before editing a kit."));
            return;
        }

        String kitName = "Kit " + kitNumber;
        String guiTitle = ColorizeText.hex("&8Editing " + kitName);
        GuiBuilder sessionInventory = new GuiBuilder(9, guiTitle);
        KitEditorSessionManager.startSession(player, kitName, guiTitle, sessionInventory, false);
        KitsX.getKitUtil().loadForEditor(player, kitName);

        sendConfigMessage(player, "messages.kit_edit_started", "&#7cff6eEditing %kit%.", kitName, kitCommandName(kitNumber));
        sendConfigMessage(player, "messages.kit_edit_save_hint",
                "&#f8ff9cUse /customkit for items, /%kitcmd% save to save, or /kitcancel to cancel.",
                kitName,
                kitCommandName(kitNumber));
    }

    private static void sendConfigMessage(Player player, String path, String fallback) {
        sendConfigMessage(player, path, fallback, "", "");
    }

    private static void sendConfigMessage(Player player, String path, String fallback, String kitName, String kitCommand) {
        String message = KitsX.getInstance().getConfig().getString(path, fallback);
        message = message.replace("%kit%", kitName).replace("%kitcmd%", kitCommand);
        player.sendMessage(ColorizeText.hex(message));
    }

    private static String kitCommandName(int kitNumber) {
        return "k" + kitNumber;
    }

    private static void addKitRoomItem(GuiBuilder inventory, Player player) {
        addItem(inventory, "kits_menu.kitroom", Material.CREEPER_BANNER_PATTERN, player);
    }

    private static void addClearInventoryItem(GuiBuilder inventory, Player player) {
        addItem(inventory, "kits_menu.clearinv", Material.RED_DYE, player);
    }

    @SuppressWarnings("deprecation")
    private static void addItem(GuiBuilder inventory, String configName, @NotNull Material defaultMaterial, Player player) {
        String itemMaterial = CONFIG.getConfig().getString(configName + ".material", defaultMaterial.name());
        String itemName = CONFIG.getConfig().getString(configName + ".name", "");
        int itemSlot = CONFIG.getConfig().getInt(configName + ".slot");
        List<String> loreList = CONFIG.getConfig().getStringList(configName + ".lore");
        List<String> finalLore = new ArrayList<>();
        for (String lore : loreList) {
            finalLore.add(ColorizeText.hex(lore));
        }
        List<String> flagList = CONFIG.getConfig().getStringList(configName + ".flags");
        List<ItemFlag> flags = new ArrayList<>();
        for (String flag : flagList) {
            try {
                flags.add(ItemFlag.valueOf(flag));
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid item flag " + flag);
            }
        }
        List<Map<?, ?>> enchantmentList = CONFIG.getConfig().getMapList(configName + ".enchantments");
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (Map<?, ?> enchantmentMap : enchantmentList) {
            try {
                String type = (String) enchantmentMap.get("type");
                int level = (Integer) enchantmentMap.get("level");
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(type.toLowerCase()));
                if (enchantment != null) {
                    enchantments.put(enchantment, level);
                } else {
                    LOGGER.warning("Invalid enchantment type " + type);
                }
            } catch (ClassCastException e) {
                LOGGER.warning("Invalid enchantment data format for " + configName);
            }
        }

        ItemStack item = new ItemBuilderGUI(Material.valueOf(itemMaterial))
                .name(ColorizeText.hex(itemName))
                .flags(flags.toArray(new ItemFlag[0]))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .lore(finalLore.toArray(new String[0]))
                .build();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }

        inventory.setItem(itemSlot, item, event -> {
            Player clicker = (Player) event.getWhoClicked();
            switch (configName) {
                case "kits_menu.kitroom":
                    if (KitEditorSessionManager.isEditing(clicker)) {
                        KitRoomMenu.openKitRoom(clicker).open(clicker);
                    } else {
                        clicker.sendMessage(ColorizeText.hex("&#ffa6a6Right-click a kit in the editor region before opening the item palette."));
                    }
                    break;
                case "kits_menu.clearinv":
                    if (KitEditorSessionManager.isEditing(clicker)) {
                        clicker.setItemOnCursor(new ItemStack(Material.AIR));
                    } else {
                        clicker.getInventory().clear();
                    }
                    break;
            }
        });
    }
}
