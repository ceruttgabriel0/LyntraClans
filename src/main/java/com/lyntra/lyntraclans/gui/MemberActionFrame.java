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
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

/** Tela de acoes sobre um membro especifico: promover, rebaixar, expulsar, confiar. Ações reais, não só leitura. */
public final class MemberActionFrame extends AbstractFrame {

    private final ClanServices services;
    private final Clan clan;
    private final ClanMember target;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MemberActionFrame(ClanServices services, Clan clan, ClanMember target, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.target = target;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(target.getUuid());
        String name = offlineTarget.getName() == null ? target.getUuid().toString() : offlineTarget.getName();
        return Bukkit.createInventory(this, 27, miniMessage.deserialize("<dark_gray>Ações - " + name));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(target.getUuid());
        Rank targetRank = clan.getRankById(target.getRankId());
        Optional<ClanMember> viewerMember = services.clanManager().getMember(player.getUniqueId());

        inventory.setItem(4, ItemBuilder.playerHead(offlineTarget)
                .name(Component.text(offlineTarget.getName() == null ? "?" : offlineTarget.getName()))
                .lore(Component.text("Cargo: " + (targetRank == null ? "-" : targetRank.getDisplayName())))
                .lore(Component.text("Confiável: " + (target.isTrusted() ? "Sim" : "Não")))
                .build());

        if (viewerMember.isEmpty()) {
            inventory.setItem(22, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
            return;
        }
        ClanMember viewer = viewerMember.get();
        boolean viewerIsLeader = isLeader(viewer);
        Rank viewerRank = clan.getRankById(viewer.getRankId());

        if (viewerIsLeader && !isLeader(target)) {
            inventory.setItem(10, ItemBuilder.of(Material.GOLDEN_HELMET)
                    .name(Component.text("Promover a líder"))
                    .lore(Component.text("Clique pra promover"))
                    .build());
        }
        if (viewerIsLeader && isLeader(target) && !target.getUuid().equals(viewer.getUuid())) {
            inventory.setItem(11, ItemBuilder.of(Material.IRON_HELMET)
                    .name(Component.text("Rebaixar"))
                    .lore(Component.text("Clique pra rebaixar"))
                    .build());
        }
        if (has(viewerRank, viewer, ClanPermission.EXPULSAR) && !target.getUuid().equals(viewer.getUuid())
                && (targetRank == null || viewerRank == null || targetRank.getPriority() < viewerRank.getPriority()
                || viewerIsLeader)) {
            inventory.setItem(13, ItemBuilder.of(Material.BARRIER)
                    .name(Component.text("Expulsar"))
                    .lore(Component.text("Clique pra expulsar do clã"))
                    .build());
        }
        if (has(viewerRank, viewer, ClanPermission.GERENCIAR_CONFIANCA)) {
            inventory.setItem(15, ItemBuilder.of(target.isTrusted() ? Material.GRAY_DYE : Material.LIME_DYE)
                    .name(Component.text(target.isTrusted() ? "Remover confiança" : "Marcar como confiável"))
                    .build());
        }
        if (has(viewerRank, viewer, ClanPermission.GERENCIAR_CARGOS) && !isLeader(target)) {
            inventory.setItem(16, ItemBuilder.of(Material.NAME_TAG)
                    .name(Component.text("Definir cargo"))
                    .lore(Component.text("Clique pra escolher um cargo do clã"))
                    .build());
        }
        inventory.setItem(22, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    private boolean isLeader(ClanMember member) {
        Rank highest = clan.getHighestRank();
        return highest != null && highest.getId() == member.getRankId();
    }

    private boolean has(Rank rank, ClanMember member, ClanPermission permission) {
        if (rank == null) {
            return false;
        }
        if (isLeader(member)) {
            return true;
        }
        return rank.has(permission);
    }

    @Override
    public void handleClick(Player player, int slot) {
        Optional<ClanMember> viewerMember = services.clanManager().getMember(player.getUniqueId());
        if (viewerMember.isEmpty()) {
            return;
        }
        ClanMember viewer = viewerMember.get();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

        switch (slot) {
            case 10 -> {
                if (isLeader(viewer)) {
                    services.memberManager().setRank(target, clan.getHighestRank());
                    player.sendMessage(services.languageManager().get("promover-sucesso",
                            "jogador", nameOf(target)));
                    onBack.run();
                }
            }
            case 11 -> {
                if (isLeader(viewer) && isLeader(target)) {
                    long leaderCount = services.clanManager().getMembers(clan.getId()).stream()
                            .filter(this::isLeader).count();
                    if (leaderCount <= 1) {
                        player.sendMessage(services.languageManager().get("rebaixar-unico-lider"));
                        return;
                    }
                    Rank defaultRank = clan.getDefaultRank();
                    if (defaultRank != null) {
                        services.memberManager().setRank(target, defaultRank);
                        player.sendMessage(services.languageManager().get("rebaixar-sucesso", "jogador", nameOf(target)));
                        onBack.run();
                    }
                }
            }
            case 13 -> {
                // Botao so aparece renderizado quando essas condicoes batem (ver populate()), mas o
                // clique chega no handler por slot independente do que foi desenhado - sem reconferir
                // aqui, um clique bruto no slot 13 expulsaria gente mesmo sem permissao EXPULSAR.
                Rank viewerRank = clan.getRankById(viewer.getRankId());
                Rank targetRank = clan.getRankById(target.getRankId());
                boolean viewerIsLeader = isLeader(viewer);
                boolean canKick = has(viewerRank, viewer, ClanPermission.EXPULSAR)
                        && !target.getUuid().equals(viewer.getUuid())
                        && (targetRank == null || viewerRank == null || targetRank.getPriority() < viewerRank.getPriority()
                        || viewerIsLeader);
                if (!canKick) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                services.memberManager().kick(clan, target);
                player.sendMessage(services.languageManager().get("expulsar-sucesso", "jogador", nameOf(target)));
                onBack.run();
            }
            case 15 -> {
                Rank viewerRank = clan.getRankById(viewer.getRankId());
                if (!has(viewerRank, viewer, ClanPermission.GERENCIAR_CONFIANCA)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                target.setTrusted(!target.isTrusted());
                services.clanManager().persistMember(target);
                player.sendMessage(services.languageManager().get(
                        target.isTrusted() ? "confiar-sucesso" : "naoconfiar-sucesso", "jogador", nameOf(target)));
                open(player);
            }
            case 16 -> {
                Rank viewerRank = clan.getRankById(viewer.getRankId());
                if (!has(viewerRank, viewer, ClanPermission.GERENCIAR_CARGOS) || isLeader(target)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                new RankPickerFrame(services, clan, rank -> {
                    services.memberManager().setRank(target, rank);
                    player.sendMessage(services.languageManager().get("cargo-definido",
                            "jogador", nameOf(target), "cargo", rank.getDisplayName()));
                    onBack.run();
                }, () -> open(player)).open(player);
            }
            case 22 -> onBack.run();
            default -> {
            }
        }
    }

    private String nameOf(ClanMember member) {
        String name = Bukkit.getOfflinePlayer(member.getUuid()).getName();
        return name == null ? member.getUuid().toString() : name;
    }
}
