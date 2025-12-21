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

package dev.darkxx.kitsx.utils.editor;

import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KitEditorSessionManager {

    private static final Map<UUID, KitEditorSession> SESSIONS = new ConcurrentHashMap<>();

    private KitEditorSessionManager() {
    }

    public static KitEditorSession startSession(@NotNull Player player, @NotNull String kitName, @NotNull String guiTitle, @NotNull GuiBuilder inventory) {
        endSession(player);

        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] inventorySnapshot = copyInventory(playerInventory.getContents());
        ItemStack[] armorSnapshot = copyArmor(playerInventory.getArmorContents());
        ItemStack offhandSnapshot = cloneItem(playerInventory.getItemInOffHand());

        Location anchor = player.getLocation().clone();

        playerInventory.clear();
        playerInventory.setArmorContents(new ItemStack[4]);
        playerInventory.setItemInOffHand(null);

        KitEditorSession session = new KitEditorSession(player.getUniqueId(), kitName, guiTitle, inventory,
            inventorySnapshot, armorSnapshot, offhandSnapshot, anchor);
        SESSIONS.put(player.getUniqueId(), session);
        return session;
    }

    public static boolean isEditing(@NotNull Player player) {
        return SESSIONS.containsKey(player.getUniqueId());
    }

    public static KitEditorSession getSession(@NotNull Player player) {
        return SESSIONS.get(player.getUniqueId());
    }

    public static void endSession(@NotNull Player player) {
        KitEditorSession session = SESSIONS.remove(player.getUniqueId());
        if (session != null) {
            restorePlayerInventory(player.getInventory(), session);
        }
    }

    public static void updateGui(@NotNull Player player, @NotNull GuiBuilder inventory, @NotNull String guiTitle) {
        KitEditorSession session = SESSIONS.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        session.setInventory(inventory);
        session.setGuiTitle(guiTitle);
    }

    public static void updateSession(@NotNull Player player, @NotNull String kitName, @NotNull GuiBuilder inventory, @NotNull String guiTitle) {
        KitEditorSession session = SESSIONS.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        session.setKitName(kitName);
        session.setInventory(inventory);
        session.setGuiTitle(guiTitle);
    }

    private static void restorePlayerInventory(@NotNull PlayerInventory inventory, @NotNull KitEditorSession session) {
        inventory.setContents(copyInventory(session.getInventorySnapshot()));
        inventory.setArmorContents(copyArmor(session.getArmorSnapshot()));
        inventory.setItemInOffHand(cloneItem(session.getOffhandSnapshot()));
    }

    @NotNull
    private static ItemStack[] copyInventory(@NotNull ItemStack[] contents) {
        ItemStack[] copy = Arrays.copyOf(contents, 36);
        for (int i = 0; i < copy.length; i++) {
            copy[i] = cloneItem(copy[i]);
        }
        return copy;
    }

    @NotNull
    private static ItemStack[] copyArmor(@NotNull ItemStack[] armor) {
        ItemStack[] copy = Arrays.copyOf(armor, 4);
        for (int i = 0; i < copy.length; i++) {
            copy[i] = cloneItem(copy[i]);
        }
        return copy;
    }

    private static ItemStack cloneItem(ItemStack item) {
        return item == null ? null : item.clone();
    }
}
