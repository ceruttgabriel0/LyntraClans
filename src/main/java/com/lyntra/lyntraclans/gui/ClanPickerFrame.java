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
import java.util.function.Consumer;

/** Escolher outro clã (excluindo o próprio) pra uma ação, tipo propor aliança ou declarar rival. */
public final class ClanPickerFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan ownClan;
    private final String title;
    private final Consumer<Clan> onPick;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private List<Clan> clans = List.of();

    public ClanPickerFrame(ClanServices services, Clan ownClan, String title, Consumer<Clan> onPick, Runnable onBack) {
        this.services = services;
        this.ownClan = ownClan;
        this.title = title;
        this.onPick = onPick;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 54, miniMessage.deserialize("<dark_gray>" + title));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        clans = services.clanManager().getAllClans().stream()
                .filter(c -> c.getId() != ownClan.getId())
                .sorted(Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(45)
                .toList();
        if (clans.isEmpty()) {
            inventory.setItem(22, ItemBuilder.of(Material.BARRIER)
                    .name(Component.text("Nenhum outro clã no servidor"))
                    .build());
        }
        int slot = 0;
        for (Clan clan : clans) {
            inventory.setItem(slot, ItemBuilder.clanBanner(clan.getColor())
                    .name(Component.text("[" + clan.getTag() + "] " + clan.getName()))
                    .lore(Component.text("Clique pra selecionar"))
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
        if (slot < 0 || slot >= clans.size()) {
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        onPick.accept(clans.get(slot));
    }
}
