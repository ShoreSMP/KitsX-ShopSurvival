package dev.darkxx.utils.misc;

import org.bukkit.Bukkit;

public final class Versions {
    private Versions() {
    }

    public static boolean isHigherThanOrEqualTo(String version) {
        String bukkit = Bukkit.getBukkitVersion();
        String[] parts = bukkit.split("-")[0].split("\\.");
        String[] target = version.split("\\.");
        int len = Math.max(parts.length, target.length);
        for (int i = 0; i < len; i++) {
            int a = i < parts.length ? parse(parts[i]) : 0;
            int b = i < target.length ? parse(target[i]) : 0;
            if (a > b) return true;
            if (a < b) return false;
        }
        return true;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
