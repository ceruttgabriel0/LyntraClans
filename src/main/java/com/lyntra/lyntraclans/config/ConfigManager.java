package com.lyntra.lyntraclans.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;

public final class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        mergeMissingKeys();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Servidores que ja rodaram uma versao antiga do plugin tem um config.yml em disco que
     * nunca e sobrescrito (saveDefaultConfig so grava se o arquivo nao existir). Sem isso,
     * uma opcao nova adicionada numa atualizacao (ex: upgrades.chest-slot-max) ficaria
     * faltando pra sempre nesses servidores, caindo sempre no valor padrao do codigo sem
     * o dono do servidor saber que existe ou poder configurar. Mesmo fix ja usado no
     * LanguageManager pros arquivos de idioma.
     */
    private void mergeMissingKeys() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        try (InputStream stream = plugin.getResource("config.yml")) {
            if (stream == null || !file.exists()) {
                return;
            }
            YamlConfiguration onDisk = YamlConfiguration.loadConfiguration(file);
            YamlConfiguration defaults = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (defaults.isConfigurationSection(key)) {
                    continue;
                }
                if (!onDisk.contains(key)) {
                    onDisk.set(key, defaults.get(key));
                    changed = true;
                }
            }
            if (changed) {
                onDisk.save(file);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Falha ao mesclar chaves novas em config.yml", e);
        }
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

    /** Limite de slots do baú do clã. Inventários do Bukkit só aceitam múltiplos de 9 até 54 (6 fileiras). */
    public int chestSlotMax() {
        return config.getInt("upgrades.chest-slot-max", 54);
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

    public List<String> bannedWords() {
        return config.getStringList("moderation.banned-words");
    }

    /** Segundos que um jogador precisa esperar depois de desfazer um clã antes de criar outro. */
    public long recreateCooldownSeconds() {
        return config.getLong("moderation.recreate-cooldown-seconds", 300);
    }

    /** Quanto XP o clã ganha por ponto de peso de kill (já considerando categoria + bônus de guerra). */
    public double xpPerWeightedKill() {
        return config.getDouble("leveling.xp-per-weighted-kill", 10.0);
    }

    /** XP necessário por nível, custo linear simples (nível 2 custa isso, nível 3 o dobro, etc). */
    public long xpPerLevel() {
        return config.getLong("leveling.xp-per-level", 200);
    }

    /** Quantos slots de membro extra (grátis, sem custar do banco) o clã ganha a cada nível. */
    public int memberBonusPerLevel() {
        return config.getInt("leveling.member-bonus-per-level", 1);
    }
}
