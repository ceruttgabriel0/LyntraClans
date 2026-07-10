package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.function.Consumer;

/** Escolher um cargo do clã pra atribuir a um membro - único jeito de atribuir cargo intermediário
 * (nem líder nem padrão) sem digitar comando. */
public final class RankPickerFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan clan;
    private final Consumer<Rank> onPick;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private List<Rank> ranks = List.of();

    public RankPickerFrame(ClanServices services, Clan clan, Consumer<Rank> onPick, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.onPick = onPick;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>Escolha um cargo"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        ranks = clan.getRanks();
        int slot = 0;
        for (Rank rank : ranks) {
            inventory.setItem(slot, ItemBuilder.of(rank.isDefault() ? Material.LEATHER_HELMET : Material.IRON_HELMET)
                    .name(Component.text(rank.getDisplayName()))
                    .lore(Component.text("Prioridade: " + rank.getPriority()))
                    .lore(Component.text("Clique pra atribuir"))
                    .build());
            slot++;
        }
        inventory.setItem(26, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot == 26) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
            return;
        }
        if (slot < 0 || slot >= ranks.size()) {
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        onPick.accept(ranks.get(slot));
    }
}
