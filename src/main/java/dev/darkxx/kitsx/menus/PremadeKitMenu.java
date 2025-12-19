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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PremadeKitMenu extends GuiBuilder {

    private static final KitsX PLUGIN = KitsX.getInstance();
    private static final MenuConfig CONFIG = new MenuConfig(PLUGIN, "menus/premadekit_menu.yml");

    public PremadeKitMenu() {
        super(CONFIG.getConfig().getInt("premade_kit.size"));
    }

    public static @NotNull GuiBuilder createGui(Player player, String kitName) {
        GuiBuilder inventory = new GuiBuilder(CONFIG.getConfig().getInt("premade_kit.size"), ColorizeText.hex(Objects.requireNonNull(CONFIG.getConfig().getString("premade_kit.title"))));

       KitsX.getPremadeKitUtil().set(inventory, kitName);

        addItems(inventory);
        addItem(inventory, player);

        return inventory;
    }

    private static void addItems(GuiBuilder inventory) {
        ConfigurationSection section = CONFIG.getConfig().getConfigurationSection("premade_kit.filter");
        if (section != null) {
            Material material = Material.matchMaterial(Objects.requireNonNull(section.getString("material")));
            String name = ColorizeText.hex(section.getString("name", ""));
            List<Integer> slots = section.getIntegerList("slots");
            List<String> flagList = CONFIG.getConfig().getStringList("premade_kit.filter" + ".flags");
            List<ItemFlag> flags = new ArrayList<>();
            for (String flag : flagList) {
                flags.add(ItemFlag.valueOf(flag));
            }

            ItemStack item = new ItemBuilderGUI(material)
                    .name(name)
                    .flags(flags.toArray(new ItemFlag[0]))
                    .build();

            for (int slot : slots) {
                inventory.setItem(slot, item);
            }
        }
    }

    private static void addItem(GuiBuilder inventory, Player player) {
        ConfigurationSection section = CONFIG.getConfig().getConfigurationSection("premade_kit.back");
        if (section != null) {
            Material material = Material.matchMaterial(Objects.requireNonNull(section.getString("material")));
            String name = ColorizeText.hex(section.getString("name", ""));
            int slot = section.getInt("slot");
            List<String> flagList = CONFIG.getConfig().getStringList("premade_kit.back" + ".flags");
            List<ItemFlag> flags = new ArrayList<>();
            for (String flag : flagList) {
                flags.add(ItemFlag.valueOf(flag));
            }
            ItemStack item = new ItemBuilderGUI(material)
                    .name(name)
                    .flags(flags.toArray(new ItemFlag[0]))
                    .build();

            inventory.setItem(slot, item, event -> {
                Player clicker = (Player) event.getWhoClicked();
                KitsMenu.openKitMenu(clicker).open(clicker);
            });
        }
    }
}