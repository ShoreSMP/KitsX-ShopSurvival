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
import dev.darkxx.kitsx.utils.wg.BlacklistedRegion;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KitEditorListener implements Listener {

    private static final String EDITING_BLOCKED_MESSAGE = "&#ffa6a6Editing a kit. Use /customkit for items, /k# save, or /kitcancel.";
    private static final long EDITING_BLOCKED_MESSAGE_COOLDOWN_MS = 1500L;
    private static final Map<UUID, Long> LAST_EDITING_BLOCKED_MESSAGE_AT = new ConcurrentHashMap<>();

    private static void sendEditingBlockedMessage(Player player) {
        long now = System.currentTimeMillis();
        Long last = LAST_EDITING_BLOCKED_MESSAGE_AT.get(player.getUniqueId());
        if (last != null && now - last < EDITING_BLOCKED_MESSAGE_COOLDOWN_MS) {
            return;
        }
        LAST_EDITING_BLOCKED_MESSAGE_AT.put(player.getUniqueId(), now);
        player.sendMessage(ColorizeText.hex(EDITING_BLOCKED_MESSAGE));
    }

    private static boolean isKitNumberCommand(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String lower = token.toLowerCase(Locale.ENGLISH);
        String digits;
        if (lower.startsWith("k")) {
            digits = lower.substring(1);
        } else if (lower.startsWith("kit")) {
            digits = lower.substring(3);
        } else {
            return false;
        }
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            return false;
        }
        int kitIndex;
        try {
            kitIndex = Integer.parseInt(digits);
        } catch (NumberFormatException ignored) {
            return false;
        }
        int maxKits = KitsX.getInstance().getConfig().getInt("kits", 7);
        return kitIndex >= 1 && kitIndex <= maxKits;
    }

    private static String normalizeCommandToken(String token) {
        String lower = token.toLowerCase(Locale.ENGLISH);
        int namespace = lower.indexOf(':');
        return namespace >= 0 ? lower.substring(namespace + 1) : lower;
    }

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
        if (!BlacklistedRegion.isInEditorRegion(player, to)) {
            KitEditorSessionManager.endSession(player);
            player.closeInventory();
            String message = KitsX.getInstance().getConfig().getString("messages.editor_region_required",
                    "&#ffa6a6You can only edit kits in the kit editor region.");
            player.sendMessage(ColorizeText.hex(message));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        KitEditorSession session = KitEditorSessionManager.getSession(player);
        if (session == null) {
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
        String primary = normalizeCommandToken(parts[0]);
        String second = parts.length > 1 ? normalizeCommandToken(parts[1]) : "";
        String third = parts.length > 2 ? normalizeCommandToken(parts[2]) : "";
        if (primary.equals("kitcancel") || primary.equals("customkit")) {
            return;
        }
        if (primary.equals("kitsx") && second.equals("kitcancel")) {
            return;
        }
        if (primary.equals("kitsx") && second.equals("customkit")) {
            return;
        }
        if (isKitNumberCommand(primary) && (second.equals("save") || second.equals("import"))) {
            return;
        }
        if (primary.equals("kitsx") && isKitNumberCommand(second) && (third.equals("save") || third.equals("import"))) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(ColorizeText.hex("&#ffa6a6You can only use /customkit, /kitcancel, or /k# save while editing kits."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && KitEditorSessionManager.isEditing(player)) {
            event.setCancelled(true);
            sendEditingBlockedMessage(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
            sendEditingBlockedMessage(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
            sendEditingBlockedMessage(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && KitEditorSessionManager.isEditing(victim)) {
            event.setCancelled(true);
            sendEditingBlockedMessage(victim);
            return;
        }

        Player attacker = resolveResponsiblePlayer(event.getDamager());
        if (attacker != null && KitEditorSessionManager.isEditing(attacker)) {
            event.setCancelled(true);
            sendEditingBlockedMessage(attacker);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && KitEditorSessionManager.isEditing(player)) {
            event.setCancelled(true);
            sendEditingBlockedMessage(player);
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
        sendEditingBlockedMessage(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
            sendEditingBlockedMessage(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
            sendEditingBlockedMessage(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (KitEditorSessionManager.isEditing(event.getPlayer())) {
            event.setCancelled(true);
            sendEditingBlockedMessage(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !KitEditorSessionManager.isEditing(player)) {
            return;
        }
        if (!isUnsafeCraftingSlot(event)) {
            return;
        }
        event.setCancelled(true);
        KitEditorSessionManager.flushCraftingSlots(player, event.getView().getTopInventory());
        sendEditingBlockedMessage(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !KitEditorSessionManager.isEditing(player)) {
            return;
        }
        InventoryType topType = event.getView().getTopInventory().getType();
        int topSize = event.getView().getTopInventory().getSize();
        boolean touchesCraftingSlot = event.getRawSlots().stream()
            .anyMatch(slot -> isRawCraftingSlot(topType, slot, topSize));
        if (!touchesCraftingSlot) {
            return;
        }
        event.setCancelled(true);
        KitEditorSessionManager.flushCraftingSlots(player, event.getView().getTopInventory());
        sendEditingBlockedMessage(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !KitEditorSessionManager.isEditing(player)) {
            return;
        }
        event.setCancelled(true);
        KitEditorSessionManager.flushCraftingSlots(player);
        sendEditingBlockedMessage(player);
    }

    @EventHandler(ignoreCancelled = false)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player) || !KitEditorSessionManager.isEditing(player)) {
            return;
        }
        event.getInventory().setResult(new ItemStack(Material.AIR));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!KitEditorSessionManager.isEditing(player)) {
            return;
        }
        KitEditorSessionManager.flushCraftingSlots(player, event.getInventory());
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            player.setItemOnCursor(new ItemStack(Material.AIR));
        }
        KitEditorSessionManager.setPaletteOpen(player, false);
    }

    private boolean isUnsafeCraftingSlot(InventoryClickEvent event) {
        InventoryType.SlotType slotType = event.getSlotType();
        if (slotType == InventoryType.SlotType.CRAFTING || slotType == InventoryType.SlotType.RESULT) {
            return true;
        }
        InventoryType topType = event.getView().getTopInventory().getType();
        int topSize = event.getView().getTopInventory().getSize();
        return isRawCraftingSlot(topType, event.getRawSlot(), topSize);
    }

    private boolean isRawCraftingSlot(InventoryType topType, int rawSlot, int topSize) {
        return (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH)
            && rawSlot >= 0
            && rawSlot < topSize;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!KitEditorSessionManager.isEditing(player)) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        LAST_EDITING_BLOCKED_MESSAGE_AT.remove(player.getUniqueId());
        KitEditorSessionManager.endSession(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        LAST_EDITING_BLOCKED_MESSAGE_AT.remove(event.getPlayer().getUniqueId());
        KitEditorSessionManager.endSession(event.getPlayer());
    }

    private Player resolveResponsiblePlayer(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            return shooter instanceof Player player ? player : null;
        }
        if (damager instanceof TNTPrimed tnt && tnt.getSource() instanceof Player player) {
            return player;
        }
        return null;
    }
}
