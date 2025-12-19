package dev.darkxx.utils.server;

import org.bukkit.Server;
import org.bukkit.Bukkit;

public final class Servers {
    private Servers() {
    }

    public static Server server() {
        return Bukkit.getServer();
    }
}
