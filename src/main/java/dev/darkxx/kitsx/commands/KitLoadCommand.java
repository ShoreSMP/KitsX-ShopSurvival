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
import dev.darkxx.kitsx.utils.InventorySnapshot;
import dev.darkxx.kitsx.utils.editor.KitEditorSession;
import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.kitsx.utils.wg.BlacklistedRegion;
import dev.darkxx.utils.command.XyrisCommand;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class KitLoadCommand extends XyrisCommand<KitsX> {

    private final int kitIndex;

    public KitLoadCommand(KitsX plugin, String name, int kit) {
        super(plugin, "kitsx", name);
        this.kitIndex = kit;
        setAliases("k" + kit);
        setUsage("");
        registerCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length > 0 && isSaveAlias(args[0])) {
            saveEditedKit(player);
            return true;
        }

        if (KitEditorSessionManager.isEditing(player)) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6Finish your kit edit with /k# save or /kitcancel before loading kits."));
            return true;
        }

        if (WorldGuardHook.get().isEnabled()) {
            if (BlacklistedRegion.isInBlacklistedRegion(player)) {
                String cannotUseHere = Objects.requireNonNull(KitsX.getInstance().getConfig().getString("messages.blacklisted_region"));
                player.sendMessage(ColorizeText.hex(cannotUseHere));
                return true;
            }
        }

        int kits = plugin.getConfig().getInt("kits", 7);

        String cmdName = command.getName();

        for (int i = 1; i <= kits; i++) {
            if (cmdName.equalsIgnoreCase("kit" + i)) {
                if (player.hasPermission("kitsx." + cmdName)) {
                    KitsX.getKitUtil().load(player, "Kit " + i);
                } else {
                    player.sendMessage(ColorizeText.hex("&#ffa6a6You don't have permission to use Kit " + i + "."));
                }
                return true;
            }
        }
        return false;
    }

    private void saveEditedKit(Player player) {
        KitEditorSession session = KitEditorSessionManager.getSession(player);
        if (session == null) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6You must be editing a kit to use /k# save."));
            return;
        }

        String targetKit = "Kit " + kitIndex;
        if (!targetKit.equals(session.getKitName())) {
            String message = KitsX.getInstance().getConfig().getString("messages.kit_edit_wrong_kit",
                    "&#ffa6a6You are editing %kit%. Use /%kitcmd% save for that kit.");
            player.sendMessage(ColorizeText.hex(message
                    .replace("%kit%", session.getKitName())
                    .replace("%kitcmd%", saveCommandFor(session))));
            return;
        }

        if (!player.hasPermission("kitsx.kit" + kitIndex)) {
            player.sendMessage(ColorizeText.hex("&#ffa6a6You don't have permission to save " + targetKit + "."));
            return;
        }

        InventorySnapshot snapshot = InventorySnapshot.fromPlayer(player);
        KitsX.getKitUtil().saveSnapshot(player, targetKit,
                snapshot.getStorageContents(),
                snapshot.getArmorContents(),
                snapshot.getOffhandItem());
        KitEditorSessionManager.endSession(player);
        player.closeInventory();
    }

    private boolean isSaveAlias(String arg) {
        return arg.equalsIgnoreCase("save") || arg.equalsIgnoreCase("import");
    }

    private String saveCommandFor(KitEditorSession session) {
        String kitName = session.getKitName().trim();
        if (kitName.toLowerCase().startsWith("kit ")) {
            String suffix = kitName.substring(4).trim();
            if (!suffix.isEmpty() && suffix.chars().allMatch(Character::isDigit)) {
                return "k" + suffix;
            }
        }
        return "k#";
    }
}
