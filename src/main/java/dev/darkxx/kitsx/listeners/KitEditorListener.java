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

package dev.darkxx.kitsx.listeners;

import dev.darkxx.kitsx.KitsX;
import dev.darkxx.kitsx.utils.editor.KitEditorSession;
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;

public final class KitEditorListener implements Listener {

    private static final double MAX_MOVE_DISTANCE_SQ = 1.0;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        KitEditorSession session = KitEditorSessionManager.getSession(player);
        if (session == null) {
            return;
        }
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        Location anchor = session.getAnchor();
        if (!anchor.getWorld().equals(to.getWorld())) {
            event.setTo(anchor);
            return;
        }
        if (to.distanceSquared(anchor) > MAX_MOVE_DISTANCE_SQ) {
            event.setTo(anchor);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (KitEditorSessionManager.getSession(player) == null) {
            return;
        }
        String message = event.getMessage().trim();
        if (message.startsWith("/")) {
            message = message.substring(1);
        }
        String[] parts = message.split("\\s+");
        if (parts.length == 0) {
            return;
        }
        String primary = parts[0].toLowerCase(Locale.ENGLISH);
        String second = parts.length > 1 ? parts[1].toLowerCase(Locale.ENGLISH) : "";
        String third = parts.length > 2 ? parts[2].toLowerCase(Locale.ENGLISH) : "";
        if (primary.equals("kitcancel") || primary.equals("customkit")) {
            return;
        }
        if (primary.equals("kitsx") && second.equals("kitcancel")) {
            return;
        }
        if (primary.equals("kitsx") && second.equals("customkit")) {
            return;
        }
        if ((primary.equals("k1") || primary.equals("kit1")) && second.equals("import")) {
            return;
        }
        if (primary.equals("kitsx") && (second.equals("k1") || second.equals("kit1")) && third.equals("import")) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(ColorizeText.hex("&#ffa6a6You can only use /kitcancel or /k1 import while editing kits."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && KitEditorSessionManager.isEditing(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && KitEditorSessionManager.isEditing(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!KitEditorSessionManager.isEditing(player)) {
            return;
        }
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        KitEditorSessionManager.endSession(event.getPlayer());
    }
}
