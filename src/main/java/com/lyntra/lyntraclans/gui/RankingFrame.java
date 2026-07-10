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

public final class RankingFrame extends AbstractFrame {

    private final ClanServices services;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public RankingFrame(ClanServices services, Runnable onBack) {
        this.services = services;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>Ranking de Clãs"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        List<Clan> clans = services.clanManager().getAllClans().stream()
                .sorted(Comparator.comparingDouble((Clan c) -> services.killManager().clanWeightedKdr(c)).reversed())
                .limit(18)
                .toList();
        int slot = 0;
        for (Clan clan : clans) {
            inventory.setItem(slot, ItemBuilder.clanBanner(clan.getColor())
                    .name(Component.text("#" + (slot + 1) + " [" + clan.getTag() + "] " + clan.getName()))
                    .lore(services.languageManager().get("ranking-item", "posicao", String.valueOf(slot + 1),
                            "tag", clan.getTag(), "nome", clan.getName(),
                            "kdr", String.format("%.2f", services.killManager().clanWeightedKdr(clan))))
                    .build());
            slot++;
        }
        inventory.setItem(22, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot == 22) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
        }
    }
}
