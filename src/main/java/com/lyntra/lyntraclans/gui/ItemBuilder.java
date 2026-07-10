package com.lyntra.lyntraclans.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ItemBuilder {

    private final ItemStack item;
    private final List<Component> lore = new ArrayList<>();
    private Component name;
    private OfflinePlayer skullOwner;

    private ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    /** Cabeça com a skin real do jogador (API padrao do Bukkit, sem textura hardcoded). */
    public static ItemBuilder playerHead(OfflinePlayer player) {
        ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD);
        builder.skullOwner = player;
        return builder;
    }

    /** Bandeira colorida representando um clã, usando a cor configurada do clã. */
    public static ItemBuilder clanBanner(String clanColorName) {
        DyeColor dyeColor = mapToDyeColor(clanColorName);
        ItemBuilder builder = new ItemBuilder(Material.valueOf(dyeColor.name() + "_BANNER"));
        return builder;
    }

    private static DyeColor mapToDyeColor(String colorName) {
        if (colorName == null) {
            return DyeColor.WHITE;
        }
        String normalized = colorName.toUpperCase(Locale.ROOT);
        try {
            return switch (normalized) {
                case "DARK_PURPLE" -> DyeColor.PURPLE;
                case "LIGHT_PURPLE" -> DyeColor.MAGENTA;
                case "DARK_AQUA" -> DyeColor.CYAN;
                case "DARK_GREEN" -> DyeColor.GREEN;
                case "DARK_BLUE" -> DyeColor.BLUE;
                case "DARK_RED" -> DyeColor.RED;
                case "DARK_GRAY" -> DyeColor.GRAY;
                case "GOLD" -> DyeColor.ORANGE;
                default -> DyeColor.valueOf(normalized);
            };
        } catch (IllegalArgumentException e) {
            return DyeColor.WHITE;
        }
    }

    public ItemBuilder name(Component name) {
        this.name = name.decoration(TextDecoration.ITALIC, false);
        return this;
    }

    public ItemBuilder lore(Component line) {
        this.lore.add(line.decoration(TextDecoration.ITALIC, false));
        return this;
    }

    public ItemStack build() {
        ItemMeta meta = item.getItemMeta();
        if (name != null) {
            meta.displayName(name);
        }
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        if (skullOwner != null && meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(skullOwner);
        }
        item.setItemMeta(meta);
        return item;
    }
}
