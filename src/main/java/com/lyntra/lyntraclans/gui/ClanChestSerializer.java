package com.lyntra.lyntraclans.gui;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Serializa o conteudo do bau do cla como YAML (usa a serializacao nativa do Bukkit pra
 * ItemStack via ConfigurationSerializable, sem lib externa). Guarda so os slots ocupados,
 * reconstrucao usa o tamanho atual do bau (que pode ter crescido por upgrade).
 */
public final class ClanChestSerializer {

    private ClanChestSerializer() {
    }

    public static String serialize(ItemStack[] contents) {
        YamlConfiguration config = new YamlConfiguration();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() != org.bukkit.Material.AIR) {
                config.set("items." + i, contents[i]);
            }
        }
        return config.saveToString();
    }

    public static ItemStack[] deserialize(String raw, int size) {
        ItemStack[] contents = new ItemStack[size];
        if (raw == null || raw.isBlank()) {
            return contents;
        }
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(raw);
        } catch (Exception e) {
            return contents;
        }
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) {
            return contents;
        }
        for (String key : section.getKeys(false)) {
            try {
                int index = Integer.parseInt(key);
                if (index >= 0 && index < size) {
                    contents[index] = section.getItemStack(key);
                }
            } catch (NumberFormatException ignored) {
                // chave inesperada no yaml, ignora esse slot
            }
        }
        return contents;
    }
}
