package dev.darkxx.utils.text.color;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorizeText {

    private static final Pattern HEX_PATTERN = Pattern.compile("&[#]([A-Fa-f0-9]{6})");

    private ColorizeText() {
    }

    public static String hex(String input) {
        if (input == null) {
            return null;
        }
        String withHex = applyHex(input);
        return ChatColor.translateAlternateColorCodes('&', withHex);
    }

    public static String mm(String input) {
        if (input == null) return null;
        return hex(input.replace("<", "&").replace(">", ""));
    }

    private static String applyHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(sb, ChatColor.of("#" + color).toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
