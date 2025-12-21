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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class KitEditorSession {

    private final UUID playerId;
    private String kitName;
    private String guiTitle;
    private GuiBuilder inventory;
    private final ItemStack[] inventorySnapshot;
    private final ItemStack[] armorSnapshot;
    private final ItemStack offhandSnapshot;
    private final Location anchor;

    public KitEditorSession(@NotNull UUID playerId,
                             @NotNull String kitName,
                             @NotNull String guiTitle,
                             @NotNull GuiBuilder inventory,
                             @NotNull ItemStack[] inventorySnapshot,
                             @NotNull ItemStack[] armorSnapshot,
                             ItemStack offhandSnapshot,
                             @NotNull Location anchor) {
        this.playerId = playerId;
        this.kitName = kitName;
        this.guiTitle = guiTitle;
        this.inventory = inventory;
        this.inventorySnapshot = inventorySnapshot;
        this.armorSnapshot = armorSnapshot;
        this.offhandSnapshot = offhandSnapshot;
        this.anchor = anchor.clone();
    }

    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }

    @NotNull
    public String getKitName() {
        return kitName;
    }

    public void setKitName(@NotNull String kitName) {
        this.kitName = kitName;
    }

    @NotNull
    public String getGuiTitle() {
        return guiTitle;
    }

    public void setGuiTitle(@NotNull String guiTitle) {
        this.guiTitle = guiTitle;
    }

    @NotNull
    public GuiBuilder getInventory() {
        return inventory;
    }

    public void setInventory(@NotNull GuiBuilder inventory) {
        this.inventory = inventory;
    }

    @NotNull
    public ItemStack[] getInventorySnapshot() {
        return inventorySnapshot;
    }

    @NotNull
    public ItemStack[] getArmorSnapshot() {
        return armorSnapshot;
    }

    public ItemStack getOffhandSnapshot() {
        return offhandSnapshot;
    }

    @NotNull
    public Location getAnchor() {
        return anchor.clone();
    }
}
