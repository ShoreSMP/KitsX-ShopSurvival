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

package dev.darkxx.kitsx.commands.admin;

import dev.darkxx.kitsx.KitsX;
import dev.darkxx.kitsx.menus.admin.KitViewMenu;
import dev.darkxx.utils.command.XyrisCommand;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KitsAdminCommand extends XyrisCommand<KitsX> {

    public KitsAdminCommand(KitsX plugin) {
        super(plugin, "kitsx", "kitsadmin");
        addTabbComplete(0, "view");
        addTabbComplete(0, "clear");
        addTabbComplete(0, "cooldown");
        setUsage("");
        setPermission("kitsx.admin");
        registerCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player executor)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin <view/clear/cooldown> ..."));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "view":
                if (args.length < 2) {
                    sender.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin view <player>"));
                    return true;
                }
                OfflinePlayer viewTarget = Bukkit.getOfflinePlayer(args[1]);
                if (!viewTarget.hasPlayedBefore() && !viewTarget.isOnline()) {
                    executor.sendMessage(ColorizeText.hex("&#ffa6a6Player not found."));
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin view <player>"));
                    return true;
                }
                KitViewMenu.openKitSelectMenu(executor, viewTarget.getName());
                break;
            case "clear":
                if (args.length < 2) {
                    sender.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin clear <player> <kit name>"));
                    return true;
                }
                OfflinePlayer clearTarget = Bukkit.getOfflinePlayer(args[1]);
                if (!clearTarget.hasPlayedBefore() && !clearTarget.isOnline()) {
                    executor.sendMessage(ColorizeText.hex("&#ffa6a6Player not found."));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin clear <player> <kit name>"));
                    return true;
                }
                String kitName = Stream.of(args).skip(2).collect(Collectors.joining(" "));
                KitsX.getKitUtil().delete((Player) clearTarget, kitName);
                executor.sendMessage(ColorizeText.hex("&#7cff6e" + kitName + " has been cleared for player " + clearTarget.getName() + "."));
                break;
            case "cooldown":
                handleCooldownSubcommand(executor, args);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(CommandSender sender, Command command, String alias, String @NotNull [] args) {
        if (args.length == 2 && (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("clear"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("clear")) {
            int numKits = KitsX.getInstance().getConfig().getInt("kits");
            return IntStream.rangeClosed(1, numKits)
                    .mapToObj(i -> "Kit " + i)
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("cooldown")) {
            return List.of("get", "set", "reset");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("cooldown") && args[1].equalsIgnoreCase("reset")) {
            return Stream.concat(Bukkit.getOnlinePlayers().stream().map(Player::getName), Stream.of("all"))
                    .collect(Collectors.toList());
        }
        return super.onTabComplete(sender, command, alias, args);
    }

    private void handleCooldownSubcommand(Player executor, String[] args) {
        if (args.length < 2) {
            executor.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin cooldown <get/set/reset> [value/player|all]"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "get": {
                String raw = KitsX.getInstance().getConfig().getString("cooldowns.kit_load", "0");
                long millis = KitsX.getKitUtil().parseDurationMillis(raw == null ? "0" : raw);
                String pretty = KitsX.getKitUtil().formatDuration(Math.max(millis, 1000));
                executor.sendMessage(ColorizeText.hex("&#7cff6eCooldown: &#ffffff" + (raw == null ? "0" : raw) + " &#7cff6e(roughly " + pretty + ")"));
                break;
            }
            case "set": {
                if (args.length < 3) {
                    executor.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin cooldown set <duration> (e.g., 30s, 5m, 1h30m, 1d)"));
                    return;
                }
                String duration = String.join("", Stream.of(args).skip(2).collect(Collectors.toList()));
                long millis = KitsX.getKitUtil().parseDurationMillis(duration);
                if (millis < 0 && !duration.trim().equalsIgnoreCase("0")) {
                    executor.sendMessage(ColorizeText.hex("&#ffa6a6Invalid duration. Use formats like 30s, 5m, 1h30m, 1d, or 0 to disable."));
                    return;
                }
                KitsX plugin = KitsX.getInstance();
                plugin.getConfig().set("cooldowns.kit_load", duration);
                plugin.saveConfig();
                executor.sendMessage(ColorizeText.hex("&#7cff6eKit load cooldown set to &#ffffff" + duration));
                break;
            }
            case "reset": {
                if (args.length < 3) {
                    executor.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin cooldown reset <player|all>"));
                    return;
                }

                String target = args[2];
                if (target.equalsIgnoreCase("all")) {
                    KitsX.getKitUtil().resetAllCooldowns();
                    executor.sendMessage(ColorizeText.hex("&#7cff6eAll kit load cooldowns have been reset."));
                    return;
                }

                Player player = Bukkit.getPlayerExact(target);
                if (player == null) {
                    executor.sendMessage(ColorizeText.hex("&#ffa6a6Player must be online to reset their cooldown."));
                    return;
                }
                KitsX.getKitUtil().resetCooldown(player.getUniqueId());
                executor.sendMessage(ColorizeText.hex("&#7cff6eReset kit load cooldown for &#ffffff" + player.getName() + "&#7cff6e."));
                break;
            }
            default:
                executor.sendMessage(ColorizeText.hex("&#ffa6a6Usage: /kitsadmin cooldown <get/set/reset> [value/player|all]"));
                break;
        }
    }
}
