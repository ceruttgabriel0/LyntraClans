package com.lyntra.lyntraclans.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class LanguageManager {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, List<String>> lists = new HashMap<>();
    private String prefix = "";

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load(String language) {
        messages.clear();
        lists.clear();
        String resourcePath = "language/" + language + ".yml";
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
        if (!file.exists()) {
            resourcePath = "language/br.yml";
            file = new File(plugin.getDataFolder(), resourcePath);
            if (!file.exists()) {
                plugin.saveResource(resourcePath, false);
            }
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        mergeMissingKeys(yaml, resourcePath, file);

        for (String key : yaml.getKeys(false)) {
            if (yaml.isList(key)) {
                lists.put(key, yaml.getStringList(key));
            } else {
                messages.put(key, yaml.getString(key, ""));
            }
        }
        prefix = messages.getOrDefault("prefix", "");
    }

    /**
     * Servidores que ja rodaram uma versao antiga do plugin tem um arquivo de idioma em disco
     * que nunca e sobrescrito pelo {@code saveResource} (ele so grava se o arquivo nao existir).
     * Sem isso, uma chave de mensagem nova adicionada numa atualizacao ficaria faltando pra
     * sempre nesses servidores, e o jogador veria a chave crua em vez da mensagem traduzida.
     * Por isso comparamos com o arquivo padrao embutido no jar e preenchemos o que faltar.
     */
    private void mergeMissingKeys(YamlConfiguration yaml, String resourcePath, File file) {
        try (InputStream stream = plugin.getResource(resourcePath)) {
            if (stream == null) {
                return;
            }
            YamlConfiguration defaults = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            boolean changed = false;
            for (String key : defaults.getKeys(false)) {
                if (!yaml.contains(key)) {
                    yaml.set(key, defaults.get(key));
                    changed = true;
                }
            }
            if (changed) {
                yaml.save(file);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Falha ao mesclar chaves de idioma novas em " + resourcePath, e);
        }
    }

    public Component get(String key, String... keyValuePairs) {
        return miniMessage.deserialize(raw(key, keyValuePairs));
    }

    public String raw(String key, String... keyValuePairs) {
        String value = messages.getOrDefault(key, key);
        value = value.replace("{prefix}", prefix);
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            value = value.replace("{" + keyValuePairs[i] + "}", keyValuePairs[i + 1]);
        }
        return value;
    }

    public List<Component> getList(String key, String... keyValuePairs) {
        List<String> lines = lists.getOrDefault(key, List.of(key));
        List<Component> result = new ArrayList<>();
        for (String line : lines) {
            String value = line.replace("{prefix}", prefix);
            for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
                value = value.replace("{" + keyValuePairs[i] + "}", keyValuePairs[i + 1]);
            }
            result.add(miniMessage.deserialize(value));
        }
        return result;
    }
}
