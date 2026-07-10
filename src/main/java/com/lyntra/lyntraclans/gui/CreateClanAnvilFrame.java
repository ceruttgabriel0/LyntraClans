package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.config.ConfigManager;
import com.lyntra.lyntraclans.domain.Clan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cria um clã via GUI usando uma bigorna como campo de texto (API nativa do Bukkit,
 * sem lib externa), em dois passos: tag primeiro, depois nome. O texto digitado
 * pelo jogador é lido de {@link AnvilInventory#getRenameText()} quando ele clica
 * no slot de resultado (2); ver {@link AnvilInputListener} pro custo de reparo zerado.
 */
public final class CreateClanAnvilFrame extends AbstractFrame implements AnvilTextCapture {

    private enum Step { TAG, NOME }

    private final ClanServices services;
    private final Logger logger;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private Step step = Step.TAG;
    private String capturedTag;
    private String liveRenameText = "";

    public CreateClanAnvilFrame(ClanServices services, Logger logger) {
        this.services = services;
        this.logger = logger;
    }

    @Override
    protected Inventory createInventory(Player player) {
        String title = step == Step.TAG ? "Tag do clã (2-6 letras)" : "Nome do clã";
        return Bukkit.createInventory(this, InventoryType.ANVIL, miniMessage.deserialize("<dark_gray>" + title));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        String promptName = step == Step.TAG ? "Digite a tag" : "Digite o nome (tag: " + capturedTag + ")";
        inventory.setItem(0, ItemBuilder.of(Material.PAPER)
                .name(Component.text(promptName))
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

        if (step == Step.TAG) {
            confirmTag(player, text);
        } else {
            confirmName(player, text);
        }
    }

    private void confirmTag(Player player, String tag) {
        ConfigManager config = services.configManager();
        if (tag.length() < config.tagMinLength() || tag.length() > config.tagMaxLength()) {
            player.sendMessage(services.languageManager().get("criar-tag-tamanho",
                    "min", String.valueOf(config.tagMinLength()), "max", String.valueOf(config.tagMaxLength())));
            return;
        }
        if (!tag.matches("[a-zA-Z0-9]+")) {
            player.sendMessage(services.languageManager().get("criar-tag-caracteres"));
            return;
        }
        if (services.clanManager().tagInUse(tag)) {
            player.sendMessage(services.languageManager().get("criar-tag-em-uso", "tag", tag));
            return;
        }
        this.capturedTag = tag;
        this.step = Step.NOME;
        this.liveRenameText = "";
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        open(player);
    }

    private void confirmName(Player player, String name) {
        ConfigManager config = services.configManager();
        if (name.length() < config.nameMinLength() || name.length() > config.nameMaxLength()) {
            player.sendMessage(services.languageManager().get("criar-nome-tamanho",
                    "min", String.valueOf(config.nameMinLength()), "max", String.valueOf(config.nameMaxLength())));
            return;
        }
        if (services.clanManager().nameInUse(name)) {
            player.sendMessage(services.languageManager().get("criar-nome-em-uso", "nome", name));
            return;
        }
        if (config.requireCoinsToCreate() && services.vaultHook().isEnabled()) {
            double cost = config.creationCost();
            if (!services.vaultHook().has(player, cost)) {
                player.sendMessage(services.languageManager().get("criar-sem-coins",
                        "custo", services.bankManager().format(cost)));
                return;
            }
            services.vaultHook().withdraw(player, cost);
        }
        try {
            Clan clan = services.clanManager().createClan(capturedTag, name, player.getUniqueId());
            player.closeInventory();
            player.sendMessage(services.languageManager().get("criar-sucesso", "tag", clan.getTag(),
                    "nome", clan.getName()));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao criar cla pela GUI", e);
            player.sendMessage(services.languageManager().get("erro-interno"));
        }
    }
}
