package com.karelmikie3.shieldserver.util;


import net.minecraft.ChatFormat;
import net.minecraft.network.chat.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {
    private static final Map<Character, ChatFormat> formats = new HashMap<Character, ChatFormat>(){{
        put('0', ChatFormat.BLACK);
        put('1', ChatFormat.DARK_BLUE);
        put('2', ChatFormat.DARK_GREEN);
        put('3', ChatFormat.DARK_AQUA);
        put('4', ChatFormat.DARK_RED);
        put('5', ChatFormat.DARK_PURPLE);
        put('6', ChatFormat.GOLD);
        put('7', ChatFormat.GRAY);
        put('8', ChatFormat.DARK_GRAY);
        put('9', ChatFormat.BLUE);
        put('a', ChatFormat.GREEN);
        put('b', ChatFormat.AQUA);
        put('c', ChatFormat.RED);
        put('d', ChatFormat.LIGHT_PURPLE);
        put('e', ChatFormat.YELLOW);
        put('f', ChatFormat.WHITE);
        put('k', ChatFormat.OBFUSCATED);
        put('l', ChatFormat.BOLD);
        put('m', ChatFormat.STRIKETHROUGH);
        put('n', ChatFormat.UNDERLINE);
        put('o', ChatFormat.ITALIC);
        put('r', ChatFormat.RESET);

    }};

    public static Component changeToColored(String text) {
        return changeToColored(new TextComponent(text), ChatFormat.WHITE, Collections.emptySet());
    }

    public static Component changeToColored(Component component) {
        return changeToColored(component, ChatFormat.WHITE, Collections.emptySet());
    }

    public static Component changeToColored(Component component, ChatFormat defaultColor, Set<ChatFormat> defaultEffects) {
        String text = component.getText();
        ClickEvent clickEvent = component.getStyle().getClickEvent();
        HoverEvent hoverEvent = component.getStyle().getHoverEvent();

        String stripped = text.replaceAll("(?i)(?:&([0-9A-FK-OR]))+", "");

        if (stripped.trim().isEmpty())
            return new TextComponent("");

        String[] parts = text.split("(?i)(?:&([0-9A-FK-OR]))+");

        ChatFormat color = defaultColor;
        Set<ChatFormat> effects = new HashSet<>(defaultEffects);

        TextComponent output = new TextComponent("");

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0 && part.trim().isEmpty()) {
                continue;
            } else if (i == 0) {
                ChatFormat finalColor = color;
                output.append(new TextComponent(part).modifyStyle(style -> applyStyles(style, finalColor, effects)));
                continue;
            }
            Pattern pattern = Pattern.compile(".*?(?:" + Pattern.quote(parts[i - 1]) + ")(?:(?i)((?:&[0-9A-FK-OR])+))(?:" + Pattern.quote(part) + ").*?");
            Matcher matcher = pattern.matcher(text);

            if (matcher.matches()) {
                String style = matcher.group(1);
                style = style.replaceAll("&", "");
                char[] styleChars = style.toCharArray();

                for (char styleChar : styleChars) {
                    ChatFormat format = formats.get(styleChar);
                    if (format != null) {
                        if (format == ChatFormat.RESET) {
                            color = ChatFormat.WHITE;
                            effects.clear();
                        } else if (format.isModifier()) {
                            effects.add(format);
                        } else if (format.isColor()) {
                            color = format;
                        }
                    }
                }

                //System.out.println(style);
            }

            ChatFormat finalColor1 = color;
            output.append(new TextComponent(part).modifyStyle(style -> applyStyles(style, finalColor1, effects)));
            //matcher.find();
        }

        output.modifyStyle(style -> style.setClickEvent(clickEvent));
        output.modifyStyle(style -> style.setHoverEvent(hoverEvent));

        return output;
    }

    private static void applyStyles(Style style, ChatFormat color, Set<ChatFormat> effects) {
        style.setColor(color);
        style.setObfuscated(effects.contains(ChatFormat.OBFUSCATED));
        style.setBold(effects.contains(ChatFormat.BOLD));
        style.setStrikethrough(effects.contains(ChatFormat.STRIKETHROUGH));
        style.setUnderline(effects.contains(ChatFormat.UNDERLINE));
        style.setItalic(effects.contains(ChatFormat.ITALIC));
    }
}
