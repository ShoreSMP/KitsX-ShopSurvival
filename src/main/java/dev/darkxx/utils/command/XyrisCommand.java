package dev.darkxx.utils.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public abstract class XyrisCommand<T extends Plugin> implements CommandExecutor, TabCompleter {

    protected final T plugin;
    private final String commandName;
    private final List<String> aliases = new ArrayList<>();
    private final Map<Integer, List<Suggestion>> tabCompletions = new HashMap<>();
    private String permission;
    private String usage = "";

    protected XyrisCommand(T plugin, String ignoredPrefix, String commandName) {
        this.plugin = plugin;
        this.commandName = commandName;
    }

    protected void setAliases(String... aliases) {
        this.aliases.clear();
        this.aliases.addAll(Arrays.asList(aliases));
    }

    protected void setPermission(String permission) {
        this.permission = permission;
    }

    protected void setUsage(String usage) {
        this.usage = usage;
    }

    protected void addTabbComplete(int index, String... options) {
        tabCompletions.computeIfAbsent(index, k -> new ArrayList<>())
                .add(new Suggestion(null, Arrays.asList(options)));
    }

    // legacy signature with permission
    protected void addTabbComplete(int index, String permission, String[] ignored, String option) {
        tabCompletions.computeIfAbsent(index, k -> new ArrayList<>())
                .add(new Suggestion(permission, Collections.singletonList(option)));
    }

    protected void registerCommand() {
        try {
            PluginCommand cmd = create(commandName, plugin);
            if (permission != null) {
                cmd.setPermission(permission);
            }
            if (!aliases.isEmpty()) {
                cmd.setAliases(aliases);
            }
            if (usage != null && !usage.isEmpty()) {
                cmd.setUsage(usage);
            }
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
            getCommandMap().register(plugin.getName().toLowerCase(Locale.ENGLISH), cmd);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register command: " + commandName + " - " + e.getMessage());
        }
    }

    private PluginCommand create(String name, Plugin plugin) throws Exception {
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        return constructor.newInstance(name, plugin);
    }

    private CommandMap getCommandMap() throws Exception {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(Bukkit.getServer());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        int index = args.length - 1;
        List<Suggestion> suggestions = tabCompletions.get(index);
        if (suggestions == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (Suggestion s : suggestions) {
            if (s.permission != null && !sender.hasPermission(s.permission)) {
                continue;
            }
            result.addAll(s.values);
        }
        return result;
    }

    private record Suggestion(String permission, List<String> values) {
    }
}
