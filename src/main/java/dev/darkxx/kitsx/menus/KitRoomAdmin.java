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
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.menu.xmenu.ItemBuilderGUI;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class KitRoomAdmin extends GuiBuilder {

    public KitRoomAdmin() {
        super(54);
    }

    public static void openAdminKitRoom(Player player, String category) {
        GuiBuilder inventory = new GuiBuilder(54, "Editing " + category + " Kit Room");

        for (int i = 1; i <= 9; i++) {
            int slot = i + 44;
            ItemStack filter = new ItemBuilderGUI(Material.BLACK_STAINED_GLASS_PANE)
                    .name(" ")
                    .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                    .build();
            inventory.setItem(slot, filter);
        }

        KitsX.getKitRoomUtil().load(inventory, category);

        ItemStack save = new ItemBuilderGUI(Material.LIME_DYE)
                .name(ColorizeText.mm("<green>Save"))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
        inventory.setItem(48, save, event -> {
            Player clicker = (Player) event.getWhoClicked();
            KitsX.getKitRoomUtil().save(clicker, category);
            KitsMenu.openKitMenu(clicker).open(clicker);
        });

        ItemStack reset = new ItemBuilderGUI(Material.RED_DYE)
                .name(ColorizeText.mm("<red>Reset"))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
        inventory.setItem(50, reset, event -> {
            for (int i1 = 0; i1 < 45; i1++) {
                inventory.setItem(i1, new ItemStack(Material.AIR));
            }
        });

        ItemStack back = new ItemBuilderGUI(Material.RED_STAINED_GLASS_PANE)
                .name(ColorizeText.mm("<#ffa6a6>Back"))
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
                .build();
        inventory.setItem(49, back, event -> {
            Player clicker = (Player) event.getWhoClicked();
            KitsMenu.openKitMenu(clicker).open(clicker);
        });

        inventory.addClickHandler(event -> {
            int slot = event.getRawSlot();
            if (slot >= 0 && slot <= 44) {
                event.setCancelled(false);
            }
        });

        inventory.open(player);
    }
}