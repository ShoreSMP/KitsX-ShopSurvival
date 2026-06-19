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

import dev.darkxx.kitsx.utils.editor.KitEditorSessionManager;
import dev.darkxx.utils.menu.xmenu.GuiBuilder;
import dev.darkxx.utils.text.color.ColorizeText;
import org.bukkit.entity.Player;

public class KitEditorMenu extends GuiBuilder {

    public KitEditorMenu(int size) {
        super(size);
    }

    public static void openKitEditor(Player player, String kitName) {
        if (KitEditorSessionManager.isEditing(player)) {
            player.sendMessage(ColorizeText.hex("&#f8ff9cUse /customkit to open the editor item palette, then /k# save to save."));
            KitRoomMenu.openKitRoom(player).open(player);
            return;
        }
        player.sendMessage(ColorizeText.hex("&#ffa6a6Right-click a kit in the editor region to start editing."));
    }
}
