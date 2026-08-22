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

package dev.darkxx.kitsx.utils.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {
    private static ConfigManager instance;
    private final Plugin plugin;
    private final Map<String, FileConfiguration> configurations = new ConcurrentHashMap<>();

    private ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public static synchronized ConfigManager get(Plugin plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        }
        return instance;
    }

    public synchronized FileConfiguration getConfig(String filePath) {
        if (!configurations.containsKey(filePath)) {
            create(filePath);
        }
        return configurations.get(filePath);
    }

    public synchronized void saveConfig(String filePath) {
        File configFile = new File(plugin.getDataFolder(), filePath);
        FileConfiguration config = getConfig(filePath);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save config file: " + filePath);
        }
    }

    public synchronized void reloadConfig(String filePath) {
        File configFile = new File(plugin.getDataFolder(), filePath);
        configurations.put(filePath, YamlConfiguration.loadConfiguration(configFile));
    }

    public synchronized void create(String filePath) {
        File configFile = new File(plugin.getDataFolder(), filePath);
        if (!configFile.exists()) {
            plugin.saveResource(filePath, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configurations.put(filePath, config);
    }

    public synchronized void set(String filePath, String path, Object value) {
        FileConfiguration config = getConfig(filePath);
        config.set(path, value);
    }

    public synchronized Object get(String filePath, String path) {
        FileConfiguration config = getConfig(filePath);
        return config.get(path);
    }

    public synchronized boolean contains(String filePath, String path) {
        FileConfiguration config = getConfig(filePath);
        return config.contains(path);
    }

    public synchronized Object getItemStack(String filePath, String path) {
        FileConfiguration config = getConfig(filePath);
        return config.getItemStack(path);
    }

    public synchronized void remove(String filePath, String path) {
        FileConfiguration config = getConfig(filePath);
        config.set(path, null);
    }
}
