package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
import com.lyntra.lyntraclans.domain.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/** Lista os cargos do clã; clicar abre o editor de permissões daquele cargo. */
public final class RanksFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan clan;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private List<Rank> ranks = List.of();

    public RanksFrame(ClanServices services, Clan clan, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>Cargos de [" + clan.getTag() + "]"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        ranks = clan.getRanks();
        int slot = 0;
        for (Rank rank : ranks) {
            int members = (int) services.clanManager().getMembers(clan.getId()).stream()
                    .filter(m -> m.getRankId() == rank.getId()).count();
            inventory.setItem(slot, ItemBuilder.of(rank.isDefault() ? Material.LEATHER_HELMET : Material.IRON_HELMET)
                    .name(Component.text(rank.getDisplayName()))
                    .lore(Component.text("Prioridade: " + rank.getPriority()))
                    .lore(Component.text("Membros: " + members))
                    .lore(Component.text("Permissões: " + rank.getPermissions().size() + "/" + ClanPermission.values().length))
                    .lore(Component.text("Clique pra editar as permissões"))
                    .build());
            slot++;
        }
        inventory.setItem(25, ItemBuilder.of(Material.EMERALD)
                .name(Component.text("Criar cargo"))
                .lore(Component.text("Clique pra digitar o nome"))
                .build());
        inventory.setItem(26, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        if (slot == 26) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            onBack.run();
            return;
        }
        Optional<ClanMember> viewerMember = services.clanManager().getMember(player.getUniqueId());
        if (viewerMember.isEmpty()) {
            return;
        }
        ClanMember viewer = viewerMember.get();
        Rank viewerRank = clan.getRankById(viewer.getRankId());
        Rank highest = clan.getHighestRank();
        boolean isLeader = highest != null && highest.getId() == viewer.getRankId();
        boolean canManage = isLeader || (viewerRank != null && viewerRank.has(ClanPermission.GERENCIAR_CARGOS));
        if (!canManage) {
            player.sendMessage(services.languageManager().get("sem-permissao-clan"));
            return;
        }
        if (slot == 25) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AnvilTextInputFrame(services, "Nome do cargo", "Digite o nome", text -> {
                if (clan.getRanks().stream().anyMatch(r -> r.getName().equalsIgnoreCase(text))) {
                    player.sendMessage(services.languageManager().get("cargo-ja-existe", "nome", text));
                    return;
                }
                try {
                    services.rankManager().createRank(clan, text);
                    player.closeInventory();
                    player.sendMessage(services.languageManager().get("cargo-criado", "nome", text));
                } catch (SQLException e) {
                    services.logger().log(Level.SEVERE, "Falha ao criar cargo pela GUI", e);
                    player.sendMessage(services.languageManager().get("erro-interno"));
                }
            }).open(player);
            return;
        }
        if (slot < 0 || slot >= ranks.size()) {
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        new RankPermissionsFrame(services, ranks.get(slot), () -> open(player)).open(player);
    }
}
