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

import java.util.List;

/** Convidar sem precisar digitar: clica na cabeca do jogador online sem cla. */
public final class InvitePickerFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan clan;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private List<Player> candidates = List.of();

    public InvitePickerFrame(ClanServices services, Clan clan, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 54, miniMessage.deserialize("<dark_gray>Convidar jogador"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        candidates = Bukkit.getOnlinePlayers().stream()
                .map(p -> (Player) p)
                .filter(p -> services.clanManager().getClanOfPlayer(p.getUniqueId()).isEmpty())
                .filter(p -> services.playerSettingsManager().get(p.getUniqueId()).isAllowInvites())
                .limit(45)
                .toList();
        if (candidates.isEmpty()) {
            inventory.setItem(22, ItemBuilder.of(Material.BARRIER)
                    .name(Component.text("Nenhum jogador disponível"))
                    .build());
        }
        int slot = 0;
        for (Player candidate : candidates) {
            inventory.setItem(slot, ItemBuilder.playerHead(candidate)
                    .name(Component.text(candidate.getName()))
                    .lore(Component.text("Clique pra convidar"))
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
        if (slot < 0 || slot >= candidates.size()) {
            return;
        }
        Player target = candidates.get(slot);
        int currentSize = services.clanManager().getMembers(clan.getId()).size();
        if (currentSize >= clan.getMaxMembers()) {
            player.sendMessage(services.languageManager().get("convidar-clan-cheio", "atual",
                    String.valueOf(currentSize), "max", String.valueOf(clan.getMaxMembers())));
            return;
        }
        if (services.inviteManager().hasInvite(clan.getId(), target.getUniqueId())) {
            player.sendMessage(services.languageManager().get("convidar-ja-convidado", "jogador", target.getName()));
            return;
        }
        services.inviteManager().invite(clan.getId(), target.getUniqueId());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        player.sendMessage(services.languageManager().get("convidar-sucesso", "jogador", target.getName()));
        target.sendMessage(services.languageManager().get("convidar-recebido", "jogador", player.getName(),
                "tag", clan.getTag(), "nome", clan.getName()));
        open(player);
    }
}
