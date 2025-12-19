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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EnderChestEditor extends GuiBuilder {
    private static final KitsX PLUGIN = KitsX.getInstance();
    private static final MenuConfig CONFIG = new MenuConfig(PLUGIN, "menus/enderchest_editor_menu.yml");
    private static final Logger LOGGER = PLUGIN.getLogger();

    public EnderChestEditor(int size) {
        super(size);
    }

    public static void openEnderChestEditor(Player player, String kitName) {
        int inventorySize = CONFIG.getConfig().getInt("enderchest_editor.size", 36);
        String titleTemplate = CONFIG.getConfig().getString("enderchest_editor.title", "Ender Chest | %kitname%");
        String inventoryTitle = ColorizeText.hex(titleTemplate.replace("%kitname%", kitName));

        GuiBuilder inventory = new GuiBuilder(inventorySize, inventoryTitle);

        KitsX.getEnderChestUtil().set(player, kitName, inventory);

        addFilterItems(inventory);
        addItems(inventory, player, kitName);

        inventory.addClickHandler(event -> {
            int slot = event.getRawSlot();
            if (slot >= 0 && slot <= 26) {
                event.setCancelled(false);
            }
        });

        inventory.open(player);
    }

    private static void addFilterItems(GuiBuilder inventory) {
        String filterMaterial = CONFIG.getConfig().getString("enderchest_editor.filter.material", "BLACK_STAINED_GLASS_PANE");
        String filterName = CONFIG.getConfig().getString("enderchest_editor.filter.name", " ");
        List<String> filterFlagsList = CONFIG.getConfig().getStringList("enderchest_editor.filter.flags");
        List<ItemFlag> filterFlags = new ArrayList<>();
        for (String flag : filterFlagsList) {
            filterFlags.add(ItemFlag.valueOf(flag));
        }
        List<Integer> filterSlots = CONFIG.getConfig().getIntegerList("enderchest_editor.filter.slots");

        ItemStack filter = new ItemBuilderGUI(Material.valueOf(filterMaterial))
                .name(filterName)
                .flags(filterFlags.toArray(new ItemFlag[0]))
                .build();

        for (int slot : filterSlots) {
            inventory.setItem(slot, filter);
        }
    }

    private static void addItems(GuiBuilder inventory, Player player, String kitName) {
        addItem(inventory, "save", Material.LIME_DYE, player, kitName);
        addItem(inventory, "reset", Material.RED_DYE, player, kitName);
        addItem(inventory, "back", Material.RED_STAINED_GLASS_PANE, player, kitName);
    }

    @SuppressWarnings("deprecation")
    private static void addItem(GuiBuilder inventory, String configName, Material defaultMaterial, Player player, String kitName) {
        String itemMaterial = CONFIG.getConfig().getString("enderchest_editor." + configName + ".material", defaultMaterial.name());
        String itemName = CONFIG.getConfig().getString("enderchest_editor." + configName + ".name", "");
        int itemSlot = CONFIG.getConfig().getInt("enderchest_editor." + configName + ".slot");
        List<String> loreList = CONFIG.getConfig().getStringList("enderchest_editor." + configName + ".lore");
        List<String> finalLore = new ArrayList<>();
        for (String lore : loreList) {
            finalLore.add(ColorizeText.hex(lore));
        }
        List<String> flagList = CONFIG.getConfig().getStringList("enderchest_editor." + configName + ".flags");
        List<ItemFlag> flags = new ArrayList<>();
        for (String flag : flagList) {
            try {
                flags.add(ItemFlag.valueOf(flag));
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid item flag " + flag);
            }
        }
        List<Map<?, ?>> enchantmentList = CONFIG.getConfig().getMapList("enderchest_editor." + configName + ".enchantments");
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
                    KitsX.getEnderChestUtil().save(clicker, kitName);
                    KitsMenu.openKitMenu(clicker).open(clicker);
                    break;
                case "reset":
                    for (int i = 0; i <= 26; i++) {
                        inventory.setItem(i, new ItemStack(Material.AIR));
                    }
                    break;
                case "back":
                    KitsMenu.openKitMenu(clicker).open(clicker);
                    break;
            }
        });
    }
}