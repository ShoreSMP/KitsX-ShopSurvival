package dev.darkxx.utils.menu.xmenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        Inventory clicked = event.getInventory();
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
}
