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
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.utils.command.XyrisCommand;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KitCancelCommand extends XyrisCommand<KitsX> {

    public KitCancelCommand(KitsX plugin) {
        super(plugin, "kitsx", "kitcancel");
        setAliases("kc", "cancelkit");
        setUsage("");
        registerCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!KitEditorSessionManager.isEditing(player)) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6You are not editing a kit."));
            return true;
        }
        KitEditorSessionManager.endSession(player);
        player.closeInventory();
        player.sendMessage(ColorizeText.hex("&#ff2e2eKit editing has been cancelled."));
        return true;
    }
}
