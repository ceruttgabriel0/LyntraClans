package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Comparator;
import java.util.List;

public final class ClanListFrame extends AbstractFrame {

    private final ClanServices services;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private List<Clan> clans = List.of();

    public ClanListFrame(ClanServices services, Runnable onBack) {
        this.services = services;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 54, miniMessage.deserialize("<dark_gray>Lista de Clãs"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        clans = services.clanManager().getAllClans().stream()
                .sorted(Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(45)
                .toList();
        int slot = 0;
        for (Clan clan : clans) {
            int members = services.clanManager().getMembers(clan.getId()).size();
            inventory.setItem(slot, ItemBuilder.clanBanner(clan.getColor())
                    .name(Component.text("[" + clan.getTag() + "] " + clan.getName()))
                    .lore(services.languageManager().get("lista-item", "tag", clan.getTag(), "nome", clan.getName(),
                            "membros", String.valueOf(members)))
                    .build());
            slot++;
        }
        inventory.setItem(49, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot == 49) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
            return;
        }
        if (slot >= 0 && slot < clans.size()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new ClanInfoFrame(services, clans.get(slot), () -> open(player)).open(player);
        }
    }
}
