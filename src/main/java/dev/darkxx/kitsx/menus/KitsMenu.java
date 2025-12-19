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
        int inventorySize = CONFIG.getConfig().getInt("kits_menu.size", 54);
        String kitsTitle = CONFIG.getConfig().getString("kits_menu.title", "Kits");
        String inventoryTitle = ColorizeText.hex(kitsTitle);

        GuiBuilder inventory = new GuiBuilder(inventorySize, inventoryTitle);

        if (CONFIG.getConfig().getBoolean("kits_menu.filter.enabled", true)) {
            addFilterItems(inventory);
        }

        addKitItems(inventory, player);
        addEnderChestItems(inventory, player);
        addKitRoomItem(inventory, player);
        addClearInventoryItem(inventory, player);
        addPremadeKitItem(inventory, player);

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

    private static void addEnderChestItems(GuiBuilder inventory, Player player) {
        addItemGroup(inventory, "kits_menu.enderchests", Material.ENDER_CHEST, player);
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

            inventory.setItem(slot, item, e -> {
                if (e.isRightClick()) {
                    if (configPath.equals("kits_menu.kits")) {
                        KitEditorMenu.openKitEditor(player, "Kit " + kitNumber);
                    } else {
                        EnderChestEditor.openEnderChestEditor(player, "Kit " + kitNumber);
                    }
                } else if (e.isLeftClick()) {
                    KitsX.getKitUtil().load(player, "Kit " + kitNumber);
                }
            });
        }
    }

    private static void addKitRoomItem(GuiBuilder inventory, Player player) {
        addItem(inventory, "kits_menu.kitroom", Material.CREEPER_BANNER_PATTERN, player);
    }

    private static void addClearInventoryItem(GuiBuilder inventory, Player player) {
        addItem(inventory, "kits_menu.clearinv", Material.RED_DYE, player);
    }

    private static void addPremadeKitItem(GuiBuilder inventory, Player player) {
        addItem(inventory, "kits_menu.premadekit", Material.NETHERITE_CHESTPLATE, player);
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
                    KitRoomMenu.openKitRoom(clicker).open(clicker);
                    break;
                case "kits_menu.clearinv":
                    clicker.getInventory().clear();
                    break;
                case "kits_menu.premadekit":
                    PremadeKitSelectorMenu.createGui(clicker).open(clicker);
                    break;
            }
        });
    }
}
