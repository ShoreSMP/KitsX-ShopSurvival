package dev.darkxx.kitsx.utils;

import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class InventorySnapshot {

    public static final int STORAGE_SIZE = 36;
    public static final int ARMOR_SIZE = 4;
    public static final int OFFHAND_SLOT = 40;

    private final ItemStack[] storageContents;
    private final ItemStack[] armorContents;
    private final ItemStack offhandItem;

    private InventorySnapshot(@NotNull ItemStack[] storageContents,
                              @NotNull ItemStack[] armorContents,
                              ItemStack offhandItem) {
        this.storageContents = copyContents(storageContents, STORAGE_SIZE);
        this.armorContents = copyContents(armorContents, ARMOR_SIZE);
        this.offhandItem = cloneItem(offhandItem);
    }

    @NotNull
    public static InventorySnapshot empty() {
        return new InventorySnapshot(new ItemStack[STORAGE_SIZE], new ItemStack[ARMOR_SIZE], null);
    }

    @NotNull
    public static InventorySnapshot fromArrays(@NotNull ItemStack[] storageContents,
                                               @NotNull ItemStack[] armorContents,
                                               ItemStack offhandItem) {
        return new InventorySnapshot(storageContents, armorContents, offhandItem);
    }

    @NotNull
    public static InventorySnapshot fromPlayer(@NotNull Player player) {
        PlayerInventory inventory = player.getInventory();
        return new InventorySnapshot(
            inventory.getStorageContents(),
            inventory.getArmorContents(),
            inventory.getItemInOffHand()
        );
    }

    @NotNull
    public static InventorySnapshot fromInventory(@NotNull Inventory inventory) {
        ItemStack[] storage = new ItemStack[STORAGE_SIZE];
        for (int i = 0; i < STORAGE_SIZE; i++) {
            if (i < inventory.getSize()) {
                storage[i] = cloneItem(inventory.getItem(i));
            }
        }

        ItemStack[] armor = new ItemStack[ARMOR_SIZE];
        for (int i = 0; i < ARMOR_SIZE; i++) {
            int slot = STORAGE_SIZE + i;
            if (slot < inventory.getSize()) {
                armor[i] = cloneItem(inventory.getItem(slot));
            }
        }

        ItemStack offhand = OFFHAND_SLOT < inventory.getSize() ? inventory.getItem(OFFHAND_SLOT) : null;
        return new InventorySnapshot(storage, armor, offhand);
    }

    @NotNull
    public ItemStack[] getStorageContents() {
        return copyContents(storageContents, STORAGE_SIZE);
    }

    @NotNull
    public ItemStack[] getArmorContents() {
        return copyContents(armorContents, ARMOR_SIZE);
    }

    public ItemStack getOffhandItem() {
        return cloneItem(offhandItem);
    }

    public boolean isEmpty() {
        for (ItemStack item : storageContents) {
            if (!isEmpty(item)) {
                return false;
            }
        }
        for (ItemStack item : armorContents) {
            if (!isEmpty(item)) {
                return false;
            }
        }
        return isEmpty(offhandItem);
    }

    @NotNull
    public InventorySnapshot withAddedItem(@NotNull ItemStack item) {
        if (isEmpty(item)) {
            return this;
        }

        ItemStack[] storage = getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            if (isEmpty(storage[i])) {
                storage[i] = cloneItem(item);
                return new InventorySnapshot(storage, armorContents, offhandItem);
            }
        }
        return this;
    }

    public void applyToPlayer(@NotNull Player player) {
        player.setItemOnCursor(new ItemStack(Material.AIR));
        player.closeInventory();

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(new ItemStack[ARMOR_SIZE]);
        inventory.setItemInOffHand(null);

        inventory.setStorageContents(getStorageContents());
        inventory.setArmorContents(getArmorContents());
        inventory.setItemInOffHand(getOffhandItem());
        player.updateInventory();
    }

    public void applyToGui(@NotNull GuiBuilder gui) {
        applyToInventory(gui.getInventory());
    }

    public void applyToInventory(@NotNull Inventory inventory) {
        for (int i = 0; i <= OFFHAND_SLOT && i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemStack(Material.AIR));
        }

        for (int i = 0; i < storageContents.length && i < inventory.getSize(); i++) {
            inventory.setItem(i, cloneItem(storageContents[i]));
        }
        for (int i = 0; i < armorContents.length; i++) {
            int slot = STORAGE_SIZE + i;
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, cloneItem(armorContents[i]));
            }
        }
        if (OFFHAND_SLOT < inventory.getSize()) {
            inventory.setItem(OFFHAND_SLOT, cloneItem(offhandItem));
        }
    }

    @NotNull
    private static ItemStack[] copyContents(@NotNull ItemStack[] contents, int size) {
        ItemStack[] copy = Arrays.copyOf(contents, size);
        for (int i = 0; i < copy.length; i++) {
            copy[i] = cloneItem(copy[i]);
        }
        return copy;
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private static ItemStack cloneItem(ItemStack item) {
        return item == null ? null : item.clone();
    }
}
