package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

/**
 * Campo de texto genérico via bigorna (API nativa do Bukkit, sem lib externa), reaproveitável
 * pra qualquer edição de texto curto (tag, descrição, etc). Quem chama decide o que fazer com
 * o texto confirmado (validação, persistência) via {@code onSubmit}.
 */
public final class AnvilTextInputFrame extends AbstractFrame implements AnvilTextCapture {

    private final ClanServices services;
    private final String title;
    private final String promptItemName;
    private final Consumer<String> onSubmit;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private String liveRenameText = "";

    public AnvilTextInputFrame(ClanServices services, String title, String promptItemName, Consumer<String> onSubmit) {
        this.services = services;
        this.title = title;
        this.promptItemName = promptItemName;
        this.onSubmit = onSubmit;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, InventoryType.ANVIL, miniMessage.deserialize("<dark_gray>" + title));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        inventory.setItem(0, ItemBuilder.of(Material.PAPER)
                .name(Component.text(promptItemName))
                .lore(Component.text("Renomeie este item e clique no resultado"))
                .build());
    }

    public void captureRenameText(String text) {
        this.liveRenameText = text == null ? "" : text;
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot != 2) {
            return;
        }
        String text = liveRenameText.trim();
        if (text.isEmpty()) {
            player.sendMessage(services.languageManager().get("criacao-gui-texto-vazio"));
            return;
        }
        onSubmit.accept(text);
    }
}
