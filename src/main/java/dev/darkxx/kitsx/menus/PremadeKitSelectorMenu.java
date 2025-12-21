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
import dev.darkxx.kitsx.utils.config.ConfigCache;
import dev.darkxx.kitsx.utils.config.MenuConfig;
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.menu.xmenu.ItemBuilderGUI;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PremadeKitSelectorMenu {

	public static @NotNull GuiBuilder createGui(Player player) {
		MenuConfig menuConfig = new MenuConfig(KitsX.getInstance(), "menus/premadekit_selector_menu.yml");
		FileConfiguration config = menuConfig.getConfig();
		String title = (String) ConfigCache.getOrDefault("premade_kit.title",
				ColorizeText.hex(config.getString("premade_kit.title", "&6Premade Kits")));
		int size = (int) ConfigCache.getOrDefault("premade_kit.size", config.getInt("premade_kit.size", 54));

		GuiBuilder inventory = new GuiBuilder(size, title);

		if (!ConfigCache.contains("premade_kit.filter")) {
			Material filterMaterial = Material.valueOf(config.getString("premade_kit.filter.material", "BLACK_STAINED_GLASS_PANE"));
			String filterName = ColorizeText.hex(config.getString("premade_kit.filter.name", " "));
			List<Integer> filterSlots = config.getIntegerList("premade_kit.filter.slots");

			ItemStack filterItem = new ItemBuilderGUI(filterMaterial)
					.name(filterName)
					.flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
					.build();

			ConfigCache.cache("premade_kit.filter.item", filterItem);
			ConfigCache.cache("premade_kit.filter.slots", filterSlots);
		}

		List<Integer> filterSlots = (List<Integer>) ConfigCache.get("premade_kit.filter.slots");
		ItemStack filterItem = (ItemStack) ConfigCache.get("premade_kit.filter.item");
		filterSlots.forEach(slot -> inventory.setItem(slot, filterItem, event -> event.setCancelled(true)));

		if (!ConfigCache.contains("premade_kit.kits")) {
			Map<String, ItemStack> kitItems = new HashMap<>();
			ConfigurationSection kitsSection = config.getConfigurationSection("premade_kit.kits");
			if (kitsSection != null) {
				for (String kitKey : kitsSection.getKeys(false)) {
					ConfigurationSection kit = kitsSection.getConfigurationSection(kitKey);
					Material material = Material.valueOf(kit.getString("material", "CHEST"));
					String name = ColorizeText.hex(kit.getString("name", "&aUnknown Kit"));
					List<String> lore = kit.getStringList("lore");
					lore.replaceAll(ColorizeText::hex);

					ItemStack kitItem = new ItemBuilderGUI(material)
							.name(name)
							.lore(lore)
							.flags(ItemFlag.HIDE_ATTRIBUTES)
							.build();

					kitItems.put(kitKey, kitItem);
					ConfigCache.cache("premade_kit.kits." + kitKey + ".slot", kit.getInt("slot", 0));
				}
			}
			ConfigCache.cache("premade_kit.kits.items", kitItems);
		}

		Map<String, ItemStack> kitItems = (Map<String, ItemStack>) ConfigCache.get("premade_kit.kits.items");
		for (String kitKey : kitItems.keySet()) {
			int slot = (int) ConfigCache.get("premade_kit.kits." + kitKey + ".slot");
			inventory.setItem(slot, kitItems.get(kitKey), event -> {
				Player clicker = (Player) event.getWhoClicked();
				if (event.isLeftClick()) {
					KitsX.getPremadeKitUtil().load(clicker, kitKey);
					clicker.closeInventory();
				} else if (event.isRightClick()) {
					PremadeKitMenu.createGui(clicker, kitKey).open(clicker);
				}
			});
		}

		if (!ConfigCache.contains("premade_kit.back")) {
			ConfigurationSection backSection = config.getConfigurationSection("premade_kit.back");
			Material backMaterial = Material.valueOf(backSection.getString("material", "RED_STAINED_GLASS_PANE"));
			String backName = ColorizeText.hex(backSection.getString("name", "&cBack"));
			List<String> backLore = backSection.getStringList("lore");
			backLore.replaceAll(ColorizeText::hex);

			ItemStack backButton = new ItemBuilderGUI(backMaterial)
					.name(backName)
					.lore(backLore)
					.flags(ItemFlag.HIDE_ATTRIBUTES)
					.build();

			ConfigCache.cache("premade_kit.back.item", backButton);
			ConfigCache.cache("premade_kit.back.slot", backSection.getInt("slot", 49));
		}

		int backSlot = (int) ConfigCache.get("premade_kit.back.slot");
		ItemStack backButton = (ItemStack) ConfigCache.get("premade_kit.back.item");
		inventory.setItem(backSlot, backButton, event -> {
			Player clicker = (Player) event.getWhoClicked();
			KitsMenu.openKitMenu(clicker).open(clicker);
		});

		if (KitEditorSessionManager.isEditing(player)) {
			KitEditorSessionManager.updateGui(player, inventory, title);
		}

		return inventory;
	}
}