package dev.darkxx.utils.menu.xmenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GuiBuilder implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> slotHandlers = new HashMap<>();
    private final List<Consumer<InventoryClickEvent>> globalHandlers = new ArrayList<>();
    private final List<Consumer<InventoryOpenEvent>> openHandlers = new ArrayList<>();

    public GuiBuilder(int size) {
        this.inventory = Bukkit.createInventory(this, size);
    }

    public GuiBuilder(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public GuiBuilder setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        return this;
    }

    public GuiBuilder setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        inventory.setItem(slot, item);
        slotHandlers.put(slot, handler);
        return this;
    }

    public void addClickHandler(Consumer<InventoryClickEvent> handler) {
        globalHandlers.add(handler);
    }

    public void addOpenHandler(Consumer<InventoryOpenEvent> handler) {
        openHandlers.add(handler);
    }

    public Consumer<InventoryClickEvent> getSlotHandler(int slot) {
        return slotHandlers.get(slot);
    }

    public List<Consumer<InventoryClickEvent>> getGlobalHandlers() {
        return globalHandlers;
    }

    public List<Consumer<InventoryOpenEvent>> getOpenHandlers() {
        return openHandlers;
    }

    public void open(Player player) {
        player.openInventory(inventory);
        GuiManager.track(inventory, this);
    }

    void handleClose(HumanEntity human) {
        // no-op hook point
    }
}
