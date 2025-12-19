package dev.darkxx.utils.library.wrapper;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class PluginWrapper extends JavaPlugin {

    @Override
    public final void onEnable() {
        start();
    }

    @Override
    public final void onDisable() {
        stop();
    }

    protected abstract void start();

    protected abstract void stop();
}
