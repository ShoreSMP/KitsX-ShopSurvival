package dev.darkxx.utils.menu.xmenu;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemBuilderGUI {
    private final ItemStack itemStack;

    public ItemBuilderGUI(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilderGUI name(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize(name));
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilderGUI lore(String... lines) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> colored = new ArrayList<>();
            for (String line : lines) {
                colored.add(colorize(line));
            }
            meta.setLore(colored);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilderGUI lore(List<String> lines) {
        return lore(lines.toArray(new String[0]));
    }

    public ItemBuilderGUI flags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilderGUI enchantments(Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            itemStack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public ItemBuilderGUI enchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }

    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
