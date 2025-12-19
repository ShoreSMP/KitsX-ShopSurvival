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

package dev.darkxx.kitsx.hooks;

import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class HooksImpl {

    public static void of(@NotNull JavaPlugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        ConsoleCommandSender log = Bukkit.getServer().getConsoleSender();

        for (SupportedPlugins supportedPlugin : SupportedPlugins.values()) {
            if (!isHookEnabled(plugin, supportedPlugin)) {
                log.sendMessage(ColorizeText.hex("&c&lKitsX &7› &fSkipping &#ff2e2e" + supportedPlugin.getName() + " &fbecause it is disabled in config."));
                continue;
            }
            if (isPluginEnabled(pm, supportedPlugin)) {
                log.sendMessage(ColorizeText.hex("&c&lKitsX &7› &fFound &#ff2e2e" + supportedPlugin.getName() + "&f, Hooking into it"));
                supportedPlugin.hook();
                log.sendMessage(ColorizeText.hex("&c&lKitsX &7› &fHooked into &#ff2e2e" + supportedPlugin.getName() + "&f!"));
            }
        }
    }

    private static boolean isPluginEnabled(@NotNull PluginManager pluginManager, @NotNull SupportedPlugins plugin) {
        return pluginManager.isPluginEnabled(plugin.getName());
    }

    private static boolean isHookEnabled(@NotNull JavaPlugin plugin, @NotNull SupportedPlugins supportedPlugin) {
        switch (supportedPlugin) {
            case SKRIPT:
                return plugin.getConfig().getBoolean("hooks.skript", true);
            default:
                return true;
        }
    }
}