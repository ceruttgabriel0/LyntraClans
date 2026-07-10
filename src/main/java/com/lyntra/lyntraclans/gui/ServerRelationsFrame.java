package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.RelationType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

/** Todas as alianças e rivalidades do servidor, não só as do seu clã. Só leitura. */
public final class ServerRelationsFrame extends AbstractFrame {

    private final ClanServices services;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ServerRelationsFrame(ClanServices services, Runnable onBack) {
        this.services = services;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 54, miniMessage.deserialize("<dark_gray>Relações do servidor"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        List<com.lyntra.lyntraclans.managers.RelationManager.RelationPair> alliances =
                services.relationManager().getAllServerWide(RelationType.ALLY, services.clanManager());
        List<com.lyntra.lyntraclans.managers.RelationManager.RelationPair> rivalries =
                services.relationManager().getAllServerWide(RelationType.RIVAL, services.clanManager());

        for (int i = 0; i < alliances.size() && i < 9; i++) {
            var pair = alliances.get(i);
            inventory.setItem(i, ItemBuilder.of(Material.LIME_BANNER)
                    .name(Component.text("[" + pair.clanA().getTag() + "] e [" + pair.clanB().getTag() + "]"))
                    .lore(Component.text("Aliados"))
                    .build());
        }
        for (int i = 0; i < rivalries.size() && i < 9; i++) {
            var pair = rivalries.get(i);
            inventory.setItem(9 + i, ItemBuilder.of(Material.RED_BANNER)
                    .name(Component.text("[" + pair.clanA().getTag() + "] e [" + pair.clanB().getTag() + "]"))
                    .lore(Component.text("Rivais"))
                    .build());
        }
        if (alliances.isEmpty() && rivalries.isEmpty()) {
            inventory.setItem(22, ItemBuilder.of(Material.BARRIER)
                    .name(Component.text("Nenhuma relação registrada no servidor"))
                    .build());
        }
        inventory.setItem(49, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
        }
    }
}
