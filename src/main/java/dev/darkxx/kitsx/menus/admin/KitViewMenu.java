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

package dev.darkxx.kitsx.menus.admin;

import dev.darkxx.kitsx.KitsX;
import dev.darkxx.kitsx.utils.config.MenuConfig;
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.menu.xmenu.ItemBuilderGUI;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class KitViewMenu extends GuiBuilder {

    private static final KitsX PLUGIN = KitsX.getInstance();
    private static final MenuConfig CONFIG = new MenuConfig(PLUGIN, "menus/kits_menu.yml");

    public KitViewMenu() {
        super(36);
    }

    public static void openKitSelectMenu(Player executor, String targetPlayerName) {
        GuiBuilder inventory = new GuiBuilder(36, targetPlayerName + " - " + "Kits");
        List<Integer> slots = CONFIG.getConfig().getIntegerList("kits_menu.kits.slots");


        for (int s = 0; s < inventory.getInventory().getSize(); s++) {
            ItemStack filter = new ItemBuilderGUI(Material.BLACK_STAINED_GLASS_PANE)
                    .name(" ")
                    .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                    .build();
            inventory.setItem(s, filter);
        }

        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            int kitNumber = i + 1;

            ItemStack back = new ItemBuilderGUI(Material.END_CRYSTAL)
                    .name(ColorizeText.mm("<#ff2e2e>Kit " + kitNumber))
                    .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                    .build();
            inventory.setItem(slot, back, event -> kit((Player) event.getWhoClicked(), targetPlayerName, "Kit " + kitNumber));
        }

        inventory.open(executor);
    }

    public static void kit(Player executor, String targetPlayerName, String kitName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetPlayerName);
        Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
        if (targetPlayer == null) {
            return;
        }

        if (!offlinePlayer.isOnline()) {
            executor.sendMessage(ColorizeText.mm("<&#ffa6a6>Player " + targetPlayerName + " is not online."));
            return;
        }

        GuiBuilder inventory = new GuiBuilder(45, targetPlayerName + " - " + kitName);
        KitsX.getKitUtil().set(targetPlayer, kitName, inventory);

        for (int i = 1; i <= 3; i++) {
            int slot = i + 40;
            ItemStack filter = new ItemBuilderGUI(Material.BLACK_STAINED_GLASS_PANE)
                    .name(" ")
                    .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                    .build();
            inventory.setItem(slot, filter);
        }

        ItemStack back = new ItemBuilderGUI(Material.RED_STAINED_GLASS_PANE)
                .name(ColorizeText.mm("<#ffa6a6>Back"))
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                .build();
        inventory.setItem(44, back, event -> openKitSelectMenu((Player) event.getWhoClicked(), targetPlayerName));

        inventory.open(executor);
    }

}