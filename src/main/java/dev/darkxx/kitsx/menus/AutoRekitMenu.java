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
import dev.darkxx.kitsx.utils.config.MenuConfig;
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.menu.xmenu.ItemBuilderGUI;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class AutoRekitMenu extends GuiBuilder {

    private static final KitsX PLUGIN = KitsX.getInstance();
    private static final MenuConfig CONFIG = new MenuConfig(PLUGIN, "menus/autorekit_menu.yml");
    private static final Logger LOGGER = PLUGIN.getLogger();


    public AutoRekitMenu() {
        super(CONFIG.getConfig().getInt("auto_rekit.size"));
    }

    public static @NotNull GuiBuilder openAutoRekitMenu(Player player) {
        GuiBuilder inventory = new GuiBuilder(CONFIG.getConfig().getInt("auto_rekit.size", 27), CONFIG.getConfig().getString("auto_rekit.title", "Auto Rekit"));

        addFilterItems(inventory);
        addToggleAutoRekitItem(inventory, player);
        addKitItems(inventory, player);

        return inventory;
    }

    private static void addFilterItems(GuiBuilder inventory) {
        List<Integer> filterSlots = CONFIG.getConfig().getIntegerList("auto_rekit.filter.slots");

        ItemStack filter = createItem("auto_rekit.filter", Material.BLACK_STAINED_GLASS_PANE);
        for (int slot : filterSlots) {
            inventory.setItem(slot, filter);
        }
    }

    private static void addToggleAutoRekitItem(GuiBuilder inventory, Player player) {
        String configPath = "auto_rekit.toggle_auto_rekit";
        Material defaultMaterial = Material.NETHERITE_SWORD;

        int slot = CONFIG.getConfig().getInt(configPath + ".slot");
        ItemStack toggleAutoRekit = createItem(configPath, defaultMaterial);

        setToggleAutoRekitLore(toggleAutoRekit, player);

        inventory.setItem(slot, toggleAutoRekit, event -> Bukkit.getScheduler().runTaskAsynchronously(KitsX.getInstance(), () -> {
            Player clicker = (Player) event.getWhoClicked();
            KitsX.getAutoRekitUtil().toggle(clicker);
            setToggleAutoRekitLore(toggleAutoRekit, clicker);
            clicker.getOpenInventory().setItem(slot, toggleAutoRekit);
            clicker.playSound(clicker.getLocation(), Sound.UI_LOOM_TAKE_RESULT, 1.0f, 1.0f);
        }));
    }

    @SuppressWarnings("deprecation")
    private static void setToggleAutoRekitLore(ItemStack toggleAutoRekit, Player player) {
        String configPath = "auto_rekit.toggle_auto_rekit";

        List<String> lore = new ArrayList<>();
        for (String line : CONFIG.getConfig().getStringList(configPath + ".lore")) {
            lore.add(ColorizeText.hex(line.replace("%status%", KitsX.getAutoRekitUtil().status(player))));
        }
        ItemMeta meta = toggleAutoRekit.getItemMeta();
        meta.setLore(lore);
        meta.setDisplayName(ColorizeText.hex(Objects.requireNonNull(CONFIG.getConfig().getString(configPath + ".name"))));
        toggleAutoRekit.setItemMeta(meta);
    }

    private static void addKitItems(GuiBuilder inventory, Player player) {
        addItemGroup(inventory, player);
    }

    private static void addItemGroup(GuiBuilder inventory, Player player) {
        List<Integer> slots = CONFIG.getConfig().getIntegerList("auto_rekit.kits" + ".slots");

        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            int kitNumber = i + 1;

            ItemStack item = createItem("auto_rekit.kits", Material.END_CRYSTAL, kitNumber);
            inventory.setItem(slot, item, event -> {
                Player clicker = (Player) event.getWhoClicked();
                String kitSelected = PLUGIN.getConfig().getString("messages.auto_rekit_kit_selected");
                assert kitSelected != null;
                clicker.sendMessage(ColorizeText.hex(kitSelected).replace("%kit%", String.valueOf(kitNumber)));
                KitsX.getAutoRekitUtil().setKit(clicker, "Kit " + kitNumber);
            });
        }
    }

    private static ItemStack createItem(String configPath, Material defaultMaterial) {
        return createItem(configPath, defaultMaterial, 0);
    }

    @SuppressWarnings("deprecation")
    private static ItemStack createItem(String configPath, @NotNull Material defaultMaterial, int kitNumber) {
        String itemMaterial = CONFIG.getConfig().getString(configPath + ".material", defaultMaterial.name());
        String itemName = CONFIG.getConfig().getString(configPath + ".name", "").replace("%kit%", String.valueOf(kitNumber));
        List<String> loreList = CONFIG.getConfig().getStringList(configPath + ".lore");
        List<String> finalLore = new ArrayList<>();
        for (String lore : loreList) {
            finalLore.add(ColorizeText.hex(lore.replace("%i%", String.valueOf(kitNumber))));
        }
        List<String> flagList = CONFIG.getConfig().getStringList(configPath + ".flags");
        List<ItemFlag> flags = new ArrayList<>();
        for (String flag : flagList) {
            flags.add(ItemFlag.valueOf(flag));
        }

        ItemStack item = new ItemBuilderGUI(Material.valueOf(itemMaterial))
                .name(ColorizeText.hex(itemName))
                .lore(finalLore.toArray(new String[0]))
                .flags(flags.toArray(new ItemFlag[0]))
                .build();

        List<Map<?, ?>> enchantmentList = CONFIG.getConfig().getMapList(configPath + ".enchantments");
        for (Map<?, ?> enchantmentMap : enchantmentList) {
            String type = (String) enchantmentMap.get("type");
            int level = (Integer) enchantmentMap.get("level");
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(type.toLowerCase()));
            if (enchantment != null) {
                item.addUnsafeEnchantment(enchantment, level);
            } else {
                LOGGER.warning("Invalid enchantment type " + type);
            }
        }

        return item;
    }
}