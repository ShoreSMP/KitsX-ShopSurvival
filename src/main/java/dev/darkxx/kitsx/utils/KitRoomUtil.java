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

import dev.darkxx.kitsx.api.KitRoomAPI;
import dev.darkxx.kitsx.utils.config.ConfigManager;
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class KitRoomUtil implements KitRoomAPI {

    private static ConfigManager configManager;
    private final Logger logger = Logger.getLogger(KitRoomUtil.class.getName());

    public KitRoomUtil(ConfigManager configManager) {
        KitRoomUtil.configManager = configManager;
    }

    public static void of(JavaPlugin plugin) {
        configManager = ConfigManager.get(plugin);
        configManager.create("data/kitroom.yml");
    }

    @Override
    public synchronized void save(Player player, String category) {
        if (exists(category)) {
            delete(category);
        }

        for (int i = 0; i < 45; i++) {
            ItemStack item = player.getOpenInventory().getTopInventory().getItem(i);
            if (item != null) {
                configManager.set("data/kitroom.yml", "categories." + category + "." + i, item);
            }
        }

        try {
            configManager.saveConfig("data/kitroom.yml");
            player.sendMessage(ColorizeText.hex("&#7cff6eKit room category " + category + " saved!"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save category", e);
        }
    }

    @Override
    public synchronized void load(GuiBuilder inventory, String category) {
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, new ItemStack(Material.AIR));
            ItemStack item = configManager.getConfig("data/kitroom.yml").getItemStack("categories." + category + "." + i);
            if (item != null) {
                inventory.setItem(i, item);
            }
        }
    }

    @Override
    public synchronized void delete(String category) {
        if (exists(category)) {
            configManager.set("data/kitroom.yml", "categories." + category, null);
        }

        try {
            configManager.saveConfig("data/kitroom.yml");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save category", e);
        }
    }

    @Override
    public synchronized boolean exists(String category) {
        return configManager.contains("data/kitroom.yml", "categories." + category + ".");
    }
}
