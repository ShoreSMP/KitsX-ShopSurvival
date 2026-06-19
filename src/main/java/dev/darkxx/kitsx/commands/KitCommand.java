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

package dev.darkxx.kitsx.commands;

import dev.darkxx.kitsx.KitsX;
import dev.darkxx.kitsx.hooks.worldguard.WorldGuardHook;
import dev.darkxx.kitsx.menus.KitRoomMenu;
import dev.darkxx.kitsx.menus.KitsMenu;
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.kitsx.utils.wg.BlacklistedRegion;
import dev.darkxx.utils.command.XyrisCommand;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class KitCommand extends XyrisCommand<KitsX> {

    public KitCommand(KitsX plugin) {
        super(plugin, "kitsx", "customkit");
        setUsage("");
        registerCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (sender instanceof Player player) {
            if (KitEditorSessionManager.isEditing(player)) {
                KitRoomMenu.openKitRoom(player).open(player);
                return true;
            }

            if (WorldGuardHook.get().isEnabled()) {
                if (BlacklistedRegion.isInBlacklistedRegion(player)) {
                    String cannotUseHere = Objects.requireNonNull(KitsX.getInstance().getConfig().getString("messages.blacklisted_region"));
                    player.sendMessage(ColorizeText.hex(cannotUseHere));
                    return true;
                }
            }

            KitsMenu.openKitMenu(player).open(player);
            return true;
        }
        return false;
    }
}
