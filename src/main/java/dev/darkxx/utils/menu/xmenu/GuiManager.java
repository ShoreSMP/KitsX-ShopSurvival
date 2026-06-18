package dev.darkxx.utils.menu.xmenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class GuiManager implements Listener {

    private static final Map<Inventory, GuiBuilder> OPEN_INVENTORIES = new ConcurrentHashMap<>();
    private static boolean registered = false;

    private GuiManager() {
    }

    public static void register(Plugin plugin) {
        if (registered) {
            return;
        }
        Bukkit.getPluginManager().registerEvents(new GuiManager(), plugin);
        registered = true;
    }

    static void track(Inventory inventory, GuiBuilder builder) {
        OPEN_INVENTORIES.put(inventory, builder);
    }

    static void untrack(Inventory inventory) {
        OPEN_INVENTORIES.remove(inventory);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clicked = event.getView().getTopInventory();
        GuiBuilder builder = OPEN_INVENTORIES.get(clicked);
        if (builder == null) {
            return;
        }
        event.setCancelled(true);

        Consumer<InventoryClickEvent> slotHandler = builder.getSlotHandler(event.getRawSlot());
        if (slotHandler != null) {
            slotHandler.accept(event);
        }
        for (Consumer<InventoryClickEvent> handler : builder.getGlobalHandlers()) {
            handler.accept(event);
        }
        if (!event.isCancelled() && !isSafeTopInventoryEdit(event)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory opened = event.getView().getTopInventory();
        if (OPEN_INVENTORIES.containsKey(opened)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        Inventory inv = event.getInventory();
        GuiBuilder builder = OPEN_INVENTORIES.remove(inv);
        if (builder != null) {
            builder.handleClose(human);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory opened = event.getInventory();
        GuiBuilder builder = OPEN_INVENTORIES.get(opened);
        if (builder == null) {
            return;
        }
        for (Consumer<InventoryOpenEvent> handler : builder.getOpenHandlers()) {
            handler.accept(event);
        }
    }

    private boolean isSafeTopInventoryEdit(InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getView().getTopInventory().getSize()) {
            return false;
        }

        ClickType click = event.getClick();
        if (click == ClickType.SHIFT_LEFT
            || click == ClickType.SHIFT_RIGHT
            || click == ClickType.NUMBER_KEY
            || click == ClickType.SWAP_OFFHAND
            || click == ClickType.DROP
            || click == ClickType.CONTROL_DROP
            || click == ClickType.DOUBLE_CLICK
            || click == ClickType.MIDDLE
            || click == ClickType.CREATIVE) {
            return false;
        }

        InventoryAction action = event.getAction();
        return action == InventoryAction.NOTHING
            || action == InventoryAction.PICKUP_ALL
            || action == InventoryAction.PICKUP_HALF
            || action == InventoryAction.PICKUP_ONE
            || action == InventoryAction.PICKUP_SOME
            || action == InventoryAction.PLACE_ALL
            || action == InventoryAction.PLACE_ONE
            || action == InventoryAction.PLACE_SOME
            || action == InventoryAction.SWAP_WITH_CURSOR;
    }
}
