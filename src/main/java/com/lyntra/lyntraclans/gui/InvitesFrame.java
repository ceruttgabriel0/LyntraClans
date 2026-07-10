package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.storage.dao.InviteDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Convites recebidos, com aceitar/negar por clique - zero digitação. */
public final class InvitesFrame extends AbstractFrame {

    private final ClanServices services;
    private final Logger logger;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private List<Clan> pending = List.of();

    public InvitesFrame(ClanServices services, Logger logger, Runnable onBack) {
        this.services = services;
        this.logger = logger;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 54, miniMessage.deserialize("<dark_gray>Convites recebidos"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        List<Clan> result = new ArrayList<>();
        for (InviteDao.Invite invite : services.inviteManager().getInvites(player.getUniqueId())) {
            services.clanManager().getClanById(invite.clanId()).ifPresent(result::add);
        }
        pending = result;

        if (pending.isEmpty()) {
            inventory.setItem(22, ItemBuilder.of(Material.BARRIER)
                    .name(Component.text("Nenhum convite pendente"))
                    .build());
        }
        int row = 0;
        for (Clan clan : pending) {
            int base = row * 9;
            inventory.setItem(base, ItemBuilder.clanBanner(clan.getColor())
                    .name(Component.text("[" + clan.getTag() + "] " + clan.getName()))
                    .build());
            inventory.setItem(base + 1, ItemBuilder.of(Material.LIME_DYE)
                    .name(Component.text("Aceitar"))
                    .build());
            inventory.setItem(base + 2, ItemBuilder.of(Material.RED_DYE)
                    .name(Component.text("Recusar"))
                    .build());
            row++;
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
        int row = slot / 9;
        int col = slot % 9;
        if (row >= pending.size() || col > 2) {
            return;
        }
        Clan clan = pending.get(row);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

        if (col == 1) {
            accept(player, clan);
        } else if (col == 2) {
            deny(player, clan);
        }
    }

    private void accept(Player player, Clan clan) {
        if (services.clanManager().getClanOfPlayer(player.getUniqueId()).isPresent()) {
            player.sendMessage(services.languageManager().get("ja-tem-clan"));
            open(player);
            return;
        }
        if (services.clanManager().getMembers(clan.getId()).size() >= clan.getMaxMembers()) {
            player.sendMessage(services.languageManager().get("convidar-clan-cheio", "atual",
                    String.valueOf(clan.getMaxMembers()), "max", String.valueOf(clan.getMaxMembers())));
            open(player);
            return;
        }
        try {
            services.clanManager().addMember(clan, player.getUniqueId(), clan.getDefaultRank());
            services.inviteManager().removeAllInvitesForPlayer(player.getUniqueId());
            player.sendMessage(services.languageManager().get("aceitar-sucesso", "tag", clan.getTag(),
                    "nome", clan.getName()));
            broadcastJoin(clan, player);
            player.closeInventory();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Falha ao aceitar convite pela GUI", e);
            player.sendMessage(services.languageManager().get("erro-interno"));
        }
    }

    private void deny(Player player, Clan clan) {
        services.inviteManager().removeInvite(clan.getId(), player.getUniqueId());
        player.sendMessage(services.languageManager().get("negar-sucesso", "tag", clan.getTag()));
        open(player);
    }

    private void broadcastJoin(Clan clan, Player joined) {
        ClanMember joinedMember = services.clanManager().getMember(joined.getUniqueId()).orElse(null);
        services.clanManager().getMembers(clan.getId()).forEach(member -> {
            if (joinedMember != null && member.getUuid().equals(joinedMember.getUuid())) {
                return;
            }
            Player online = Bukkit.getPlayer(member.getUuid());
            if (online != null) {
                online.sendMessage(services.languageManager().get("aceitar-anuncio", "jogador", joined.getName()));
            }
        });
    }
}
