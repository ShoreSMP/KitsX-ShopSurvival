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
import dev.darkxx.kitsx.utils.editor.KitEditorSession;
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class KitEditorMenu extends GuiBuilder {

    private static final KitsX PLUGIN = KitsX.getInstance();
    private static final MenuConfig CONFIG = new MenuConfig(PLUGIN, "menus/kiteditor_menu.yml");
    private static final Logger LOGGER = PLUGIN.getLogger();


    public KitEditorMenu(int size) {
        super(size);
    }

    public static void openKitEditor(Player player, String kitName) {
        int inventorySize = CONFIG.getConfig().getInt("size", 54);
        String titleTemplate = CONFIG.getConfig().getString("title", "Editing %kitname%");
        String inventoryTitle = ColorizeText.hex(titleTemplate.replace("%kitname%", kitName));

        GuiBuilder inventory = new GuiBuilder(inventorySize, inventoryTitle);
        if (!KitEditorSessionManager.isEditing(player)) {
            KitEditorSessionManager.startSession(player, kitName, inventoryTitle, inventory);
        } else {
            KitEditorSessionManager.updateSession(player, kitName, inventory, inventoryTitle);
        }

        KitsX.getKitUtil().set(player, kitName, inventory);

        Set<Integer> blockedSlots = new HashSet<>();
        addFilter(inventory, inventorySize, blockedSlots);
        addItems(inventory, player, kitName, blockedSlots);

        inventory.addClickHandler(event -> {
            event.setCancelled(blockedSlots.contains(event.getRawSlot()));
        });

        inventory.open(player);
    }

    private static void addFilter(GuiBuilder inventory, int inventorySize, Set<Integer> blockedSlots) {
        for (int i = 1; i <= 4; i++) {
            int slot = i + (inventorySize - 14);
            ItemStack filter = new ItemBuilderGUI(Material.BLACK_STAINED_GLASS_PANE)
                    .name(" ")
                    .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                    .build();
            inventory.setItem(slot, filter);
            blockedSlots.add(slot);
            inventory.setItem(inventorySize - 9, filter);
            blockedSlots.add(inventorySize - 9);
            inventory.setItem(inventorySize - 7, filter);
            blockedSlots.add(inventorySize - 7);
            inventory.setItem(inventorySize - 6, filter);
            blockedSlots.add(inventorySize - 6);
            inventory.setItem(inventorySize - 3, filter);
            blockedSlots.add(inventorySize - 3);
            inventory.setItem(inventorySize - 1, filter);
            blockedSlots.add(inventorySize - 1);
        }
    }

    private static void addItems(GuiBuilder inventory, Player player, String kitName, Set<Integer> blockedSlots) {
        addItem(inventory, "save", Material.LIME_DYE, inventory.getInventory().getSize() - 8, player, kitName, blockedSlots);
        addItem(inventory, "reset", Material.RED_DYE, inventory.getInventory().getSize() - 6, player, kitName, blockedSlots);
        addItem(inventory, "importInventory", Material.CHEST, inventory.getInventory().getSize() - 4, player, kitName, blockedSlots);
        addItem(inventory, "premadeKit", Material.NETHERITE_CHESTPLATE, inventory.getInventory().getSize() - 2, player, kitName, blockedSlots);
        addItem(inventory, "back", Material.RED_STAINED_GLASS_PANE, inventory.getInventory().getSize() - 5, player, kitName, blockedSlots);
    }

    @SuppressWarnings("deprecation")
    private static void addItem(GuiBuilder inventory, String configName, @NotNull Material defaultMaterial, int defaultSlot, Player player, String kitName, Set<Integer> blockedSlots) {
        String itemMaterial = CONFIG.getConfig().getString("kit_editor." + configName + ".material", defaultMaterial.name());
        String itemName = CONFIG.getConfig().getString("kit_editor." + configName + ".name", "");
        int itemSlot = CONFIG.getConfig().getInt("kit_editor." + configName + ".slot", defaultSlot);
        List<String> loreList = CONFIG.getConfig().getStringList("kit_editor." + configName + ".lore");
        List<String> finalLore = new ArrayList<>();
        for (String lore : loreList) {
            finalLore.add(ColorizeText.hex(lore));
        }
        List<String> flagList = CONFIG.getConfig().getStringList("kit_editor." + configName + ".flags");
        List<ItemFlag> flags = new ArrayList<>();
        for (String flag : flagList) {
            try {
                flags.add(ItemFlag.valueOf(flag));
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid item flag " + flag);
            }
        }

        List<Map<?, ?>> enchantmentList = CONFIG.getConfig().getMapList("kit_editor." + configName + ".enchantments");
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (Map<?, ?> enchantmentMap : enchantmentList) {
            String type = (String) enchantmentMap.get("type");
            int level = (Integer) enchantmentMap.get("level");
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(type.toLowerCase()));
            if (enchantment != null) {
                enchantments.put(enchantment, level);
            } else {
                LOGGER.warning("Invalid enchantment type " + type);
            }
        }

        ItemStack item = new ItemBuilderGUI(Material.valueOf(itemMaterial))
                .name(ColorizeText.hex(itemName))
                .flags(flags.toArray(new ItemFlag[0]))
                .lore(finalLore.toArray(new String[0]))
                .build();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }

        inventory.setItem(itemSlot, item, event -> {
            Player clicker = (Player) event.getWhoClicked();
            switch (configName) {
                case "save":
                    KitsX.getKitUtil().save(clicker, kitName);
                    KitsMenu.openKitMenu(clicker).open(clicker);
                    break;
                case "reset":
                    for (int i = 0; i <= 40; i++) {
                        inventory.setItem(i, new ItemStack(Material.AIR));
                    }
                    break;
                case "importInventory":
                    KitEditorSession session = KitEditorSessionManager.getSession(clicker);
                    if (session != null) {
                        KitsX.getKitUtil().importFromSession(session.getInventory(), session);
                        KitEditorSessionManager.endSession(clicker);
                        clicker.closeInventory();
                    } else {
                        KitsX.getKitUtil().importInventory(clicker, inventory);
                    }
                    break;
                case "premadeKit":
                    PremadeKitSelectorMenu.createGui(clicker).open(clicker);
                    break;
                case "back":
                    KitsMenu.openKitMenu(clicker).open(clicker);
                    break;
            }
        });
        blockedSlots.add(itemSlot);
    }
}
