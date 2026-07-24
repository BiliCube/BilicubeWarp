package com.bilicube.bilicubeWarp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtil {

    private MessageUtil() {}

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Pattern HEX = Pattern.compile("&?#([0-9a-fA-F]{6})");

    public static String color(String input) {
        if (input == null) return "";
        Matcher m = HEX.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String hex = m.group(1);
            StringBuilder legacy = new StringBuilder("§x");
            for (char c : hex.toCharArray()) legacy.append('§').append(c);
            m.appendReplacement(sb, Matcher.quoteReplacement(legacy.toString()));
        }
        m.appendTail(sb);
        return sb.toString().replace('&', '§');
    }

    public static Component component(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        return LEGACY.deserialize(color(raw)).decoration(TextDecoration.ITALIC, false);
    }

    public static String stripHex(String raw) {
        if (raw == null) return "";
        return HEX.matcher(raw).replaceAll("");
    }

    public static Component prefixed(String raw, String prefix) {
        if (prefix == null || prefix.isEmpty()) return component(raw);
        return component(prefix).append(component(raw));
    }

    public static Component format(String template, String prefix, String... kvs) {
        String msg = color(template);
        for (int i = 0; i < kvs.length; i += 2)
            if (i + 1 < kvs.length) msg = msg.replace(kvs[i], kvs[i + 1]);
        return prefixed(msg, prefix);
    }
}
