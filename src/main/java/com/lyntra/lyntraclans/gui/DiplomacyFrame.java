package com.lyntra.lyntraclans.gui;

import com.lyntra.lyntraclans.commands.ClanServices;
import com.lyntra.lyntraclans.domain.Clan;
import com.lyntra.lyntraclans.domain.ClanMember;
import com.lyntra.lyntraclans.domain.ClanPermission;
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
import java.util.logging.Logger;

/** Aliados, rivais e guerra: tudo por clique, sem digitar tag nenhuma. */
public final class DiplomacyFrame extends AbstractFrame implements RightClickAware {

    private final ClanServices services;
    private final Clan clan;
    private final Logger logger;
    private final Runnable onBack;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private List<Clan> allies = List.of();
    private List<Clan> rivals = List.of();

    public DiplomacyFrame(ClanServices services, Clan clan, Logger logger, Runnable onBack) {
        this.services = services;
        this.clan = clan;
        this.logger = logger;
        this.onBack = onBack;
    }

    @Override
    protected Inventory createInventory(Player player) {
        return Bukkit.createInventory(this, 54, miniMessage.deserialize("<dark_gray>Diplomacia - [" + clan.getTag() + "]"));
    }

    @Override
    protected void populate(Player player, Inventory inventory) {
        allies = services.relationManager().getAllies(clan, services.clanManager());
        rivals = services.relationManager().getRivals(clan, services.clanManager());

        for (int i = 0; i < allies.size() && i < 9; i++) {
            Clan ally = allies.get(i);
            inventory.setItem(i, ItemBuilder.clanBanner(ally.getColor())
                    .name(Component.text("[" + ally.getTag() + "] " + ally.getName()))
                    .lore(Component.text("Aliado - clique pra desfazer a aliança"))
                    .build());
        }

        for (int i = 0; i < rivals.size() && i < 9; i++) {
            Clan rival = rivals.get(i);
            boolean atWar = services.warManager().isAtWar(clan.getId(), rival.getId());
            inventory.setItem(9 + i, ItemBuilder.clanBanner(rival.getColor())
                    .name(Component.text("[" + rival.getTag() + "] " + rival.getName()))
                    .lore(Component.text(atWar ? "Rival - EM GUERRA" : "Rival"))
                    .lore(Component.text("Clique esquerdo: remover rival"))
                    .lore(Component.text(atWar ? "Clique direito: finalizar guerra" : "Clique direito: declarar guerra"))
                    .build());
        }

        inventory.setItem(27, ItemBuilder.of(Material.LIME_BANNER)
                .name(Component.text("Propor aliança"))
                .lore(Component.text("Clique pra escolher um clã"))
                .build());
        inventory.setItem(28, ItemBuilder.of(Material.RED_BANNER)
                .name(Component.text("Declarar rival"))
                .lore(Component.text("Clique pra escolher um clã"))
                .build());
        inventory.setItem(29, ItemBuilder.of(Material.TNT)
                .name(Component.text("Declarar guerra"))
                .lore(Component.text("Clique pra escolher um clã rival"))
                .build());
        inventory.setItem(30, ItemBuilder.of(Material.WHITE_BANNER)
                .name(Component.text("Ver alianças/rivalidades do servidor"))
                .build());
        inventory.setItem(49, ItemBuilder.of(Material.ARROW).name(Component.text("Voltar")).build());
    }

    @Override
    public void handleClick(Player player, int slot) {
        handleClick(player, slot, false);
    }

    @Override
    public void handleRightClick(Player player, int slot) {
        handleClick(player, slot, true);
    }

    private void handleClick(Player player, int slot, boolean rightClick) {
        Optional<ClanMember> viewerMember = services.clanManager().getMember(player.getUniqueId());
        if (viewerMember.isEmpty()) {
            return;
        }
        ClanMember viewer = viewerMember.get();

        if (slot >= 0 && slot < 9 && slot < allies.size()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            if (!hasPermission(viewer, ClanPermission.GERENCIAR_ALIANCA)) {
                player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                return;
            }
            Clan ally = allies.get(slot);
            new ConfirmFrame("Desfazer aliança com [" + ally.getTag() + "]?", () -> {
                try {
                    services.relationManager().removeAlliance(clan, ally);
                    player.sendMessage(services.languageManager().get("alianca-removida", "tag", ally.getTag(),
                            "nome", ally.getName()));
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Falha ao remover alianca pela GUI", e);
                    player.sendMessage(services.languageManager().get("erro-interno"));
                }
                open(player);
            }, () -> open(player)).open(player);
            return;
        }

        if (slot >= 9 && slot < 18 && (slot - 9) < rivals.size()) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            Clan rival = rivals.get(slot - 9);
            if (rightClick) {
                if (!hasPermission(viewer, ClanPermission.GERENCIAR_GUERRA)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                boolean atWar = services.warManager().isAtWar(clan.getId(), rival.getId());
                if (atWar) {
                    services.warManager().endWar(clan, rival);
                } else {
                    services.warManager().startWar(clan, rival);
                }
                open(player);
                return;
            }
            if (!hasPermission(viewer, ClanPermission.GERENCIAR_RIVAL)) {
                player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                return;
            }
            new ConfirmFrame("Remover [" + rival.getTag() + "] da lista de rivais?", () -> {
                try {
                    services.relationManager().removeRival(clan, rival);
                    player.sendMessage(services.languageManager().get("rival-removido", "tag", rival.getTag(),
                            "nome", rival.getName()));
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Falha ao remover rival pela GUI", e);
                    player.sendMessage(services.languageManager().get("erro-interno"));
                }
                open(player);
            }, () -> open(player)).open(player);
            return;
        }

        switch (slot) {
            case 27 -> {
                if (!hasPermission(viewer, ClanPermission.GERENCIAR_ALIANCA)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ClanPickerFrame(services, clan, "Propor aliança a...", target -> {
                    try {
                        services.relationManager().proposeOrAcceptAlliance(clan, target);
                        player.sendMessage(services.languageManager().get("alianca-pedido-enviado",
                                "tag", target.getTag()));
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Falha ao propor alianca pela GUI", e);
                        player.sendMessage(services.languageManager().get("erro-interno"));
                    }
                    open(player);
                }, () -> open(player)).open(player);
            }
            case 28 -> {
                if (!hasPermission(viewer, ClanPermission.GERENCIAR_RIVAL)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ClanPickerFrame(services, clan, "Declarar rival a...", target -> {
                    try {
                        services.relationManager().declareRival(clan, target);
                        player.sendMessage(services.languageManager().get("rival-sucesso", "tag", target.getTag(),
                                "nome", target.getName()));
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Falha ao declarar rival pela GUI", e);
                        player.sendMessage(services.languageManager().get("erro-interno"));
                    }
                    open(player);
                }, () -> open(player)).open(player);
            }
            case 29 -> {
                if (!hasPermission(viewer, ClanPermission.GERENCIAR_GUERRA)) {
                    player.sendMessage(services.languageManager().get("sem-permissao-clan"));
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ClanPickerFrame(services, clan, "Declarar guerra a...", target -> {
                    boolean started = services.warManager().startWar(clan, target);
                    player.sendMessage(services.languageManager().get(
                            started ? "guerra-iniciada-anuncio" : "guerra-ja-em-guerra", "tag", target.getTag()));
                    open(player);
                }, () -> open(player)).open(player);
            }
            case 30 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new ServerRelationsFrame(services, () -> open(player)).open(player);
            }
            case 49 -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                onBack.run();
            }
            default -> {
            }
        }
    }

    private boolean hasPermission(ClanMember member, ClanPermission permission) {
        var rank = clan.getRankById(member.getRankId());
        if (rank == null) {
            return false;
        }
        var highest = clan.getHighestRank();
        if (highest != null && highest.getId() == rank.getId()) {
            return true;
        }
        return rank.has(permission);
    }
}
