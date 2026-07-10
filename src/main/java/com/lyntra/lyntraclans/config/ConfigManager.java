package com.lyntra.lyntraclans.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String language() {
        return config.getString("language", "br");
    }

    public int tagMinLength() {
        return config.getInt("clan.tag-min-length", 2);
    }

    public int tagMaxLength() {
        return config.getInt("clan.tag-max-length", 6);
    }

    public int nameMinLength() {
        return config.getInt("clan.name-min-length", 3);
    }

    public int nameMaxLength() {
        return config.getInt("clan.name-max-length", 32);
    }

    public int initialMaxMembers() {
        return config.getInt("clan.initial-max-members", 10);
    }

    public int absoluteMaxMembers() {
        return config.getInt("clan.absolute-max-members", 40);
    }

    public int initialChestSize() {
        return config.getInt("clan.initial-chest-size", 9);
    }

    public double creationCost() {
        return config.getDouble("clan.creation-cost", 1000.0);
    }

    public boolean requireCoinsToCreate() {
        return config.getBoolean("clan.require-coins-to-create", true);
    }

    public double memberSlotPrice() {
        return config.getDouble("upgrades.member-slot-price", 5000.0);
    }

    public double chestSlotPrice() {
        return config.getDouble("upgrades.chest-slot-price", 3000.0);
    }

    public int chestSlotUpgradeSize() {
        return config.getInt("upgrades.chest-slot-size", 9);
    }

    public int rankingUpdateIntervalMinutes() {
        return config.getInt("ranking.update-interval-minutes", 30);
    }

    public double killWeight(com.lyntra.lyntraclans.domain.KillCategory category) {
        return switch (category) {
            case RIVAL -> config.getDouble("kills.weight-rival", 3.0);
            case ALIADO -> config.getDouble("kills.weight-aliado", 0.0);
            case NEUTRO -> config.getDouble("kills.weight-neutro", 1.0);
            case CIVIL -> config.getDouble("kills.weight-civil", 1.0);
        };
    }

    /** Multiplicador extra aplicado ao peso do kill quando os dois clas estao em guerra ativa. */
    public double warWeightMultiplier() {
        return config.getDouble("kills.war-weight-multiplier", 1.5);
    }

    public boolean blockDamageSameClan() {
        return config.getBoolean("kills.block-damage-same-clan", true);
    }

    public boolean blockDamageAllies() {
        return config.getBoolean("kills.block-damage-allies", true);
    }

    public int clanPurgeDays() {
        return config.getInt("inactivity.clan-purge-days", 60);
    }
}
