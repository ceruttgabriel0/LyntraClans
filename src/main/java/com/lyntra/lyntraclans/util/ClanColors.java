package com.lyntra.lyntraclans.util;

import java.util.List;
import java.util.Locale;

public final class ClanColors {

    public static final List<String> VALID_COLORS = List.of(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"
    );

    private ClanColors() {
    }

    public static boolean isValid(String color) {
        return VALID_COLORS.contains(color.toLowerCase(Locale.ROOT));
    }

    public static String normalize(String color) {
        return color.toLowerCase(Locale.ROOT);
    }
}
