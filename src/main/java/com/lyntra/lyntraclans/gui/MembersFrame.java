package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public final class MembersFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan clan;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private List<ClanMember> members = List.of();

    public MembersFrame(ClanServices services, Clan clan, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 54, miniMessage.deserialize("<dark_gray>Membros - [" + clan.getTag() + "]"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        members = services.clanManager().getMembers(clan.getId());
        int slot = 0;
        for (ClanMember member : members) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
            Rank rank = clan.getRankById(member.getRankId());
            boolean online = Bukkit.getPlayer(member.getUuid()) != null;
            String name = offlinePlayer.getName() == null ? member.getUuid().toString() : offlinePlayer.getName();
            inventory.setItem(slot, ItemBuilder.playerHead(offlinePlayer)
                    .name(Component.text(name))
                    .lore(Component.text("Cargo: " + (rank == null ? "-" : rank.getDisplayName())))
                    .lore(Component.text(online ? "Online" : "Offline"))
                    .lore(Component.text(member.isTrusted() ? "Confiável" : ""))
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
        if (slot >= 0 && slot < members.size()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new MemberActionFrame(services, clan, members.get(slot), () -> open(player)).open(player);
        }
    }
}
