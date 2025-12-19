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

package dev.darkxx.kitsx.listeners;

import dev.darkxx.kitsx.KitsX;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class AutoRekitListener implements Listener {

    public static void load(Player player) {
        String kit = KitsX.getAutoRekitUtil().getKit(player);
        KitsX.getKitUtil().load(player, kit);
        if (!KitsX.getInstance().getConfig().getBoolean("messages.send_auto_rekit_message", true)) {
            return;
        }
        String msg = KitsX.getInstance().getConfig().getString("messages.auto_rekit_message");
        if (msg != null) {
            player.sendMessage(ColorizeText.hex(msg));
        }
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (!KitsX.getAutoRekitUtil().hasAutoRekit(player)) {
            KitsX.getAutoRekitUtil().set(player, false, "Kit 1");
        }
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player attacker = victim.getKiller();
        if (attacker != null) {
            if (KitsX.getAutoRekitUtil().isEnabled(attacker)) {
                load(attacker);
            }
        }
    }

}