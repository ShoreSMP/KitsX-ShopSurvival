package dev.darkxx.kitsx.listeners;

import dev.darkxx.kitsx.KitsX;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class OnlinePlayerCacheListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        KitsX.getInstance().trackOnlinePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        KitsX.getInstance().removeOnlinePlayer(event.getPlayer().getUniqueId());
    }
}
